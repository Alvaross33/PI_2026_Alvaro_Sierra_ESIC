const API = '/api';

const q = (s) => document.querySelector(s);
const show = (id) => q(id).classList.remove('hidden');
const hide = (id) => q(id).classList.add('hidden');

const state = {
  usuario: null,
  incidenciaActiva: null,
  notasInterval: null,
  usuarios: []
};

// Función para mostrar notificaciones personalizadas
function showNotification(message, duration = 3000) {
  const container = q('#custom-alert-container');
  container.textContent = message;
  container.classList.remove('hidden');
  
  setTimeout(() => {
    container.classList.add('hidden');
  }, duration);
}

// Función para validar un formulario
function validateForm(form) {
  const inputs = form.querySelectorAll('[required]');
  let isValid = true;
  
  inputs.forEach(input => {
    if (!input.value.trim()) {
      isValid = false;
      input.style.borderColor = 'var(--alert-text)';
      setTimeout(() => { input.style.borderColor = ''; }, 3000);
    }
  });
  
  if (!isValid) {
    showNotification('Por favor, rellena todos los campos obligatorios');
  }
  
  return isValid;
}

async function login(email, pass) {
  const res = await fetch(`${API}/clientes/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ correo: email, contrasena: pass }),
  });
  if (res.ok) return res.json();
  if (res.status === 403) {
    const errorMsg = await res.text();
    throw new Error(errorMsg);
  }
  if (res.status === 404 || res.status === 401) return null;
  throw new Error('Error en el servidor');
}

// Función para que un empleado asigne una incidencia a sí mismo
async function atenderIncidencia(incidenciaId, silencioso = false) {
  try {
    const res = await fetch(`${API}/incidencias/${incidenciaId}`);
    if (!res.ok) throw new Error('No se pudo obtener la incidencia');
    const inc = await res.json();

    inc.empleadoId = state.usuario.clienteId;
    inc.estado = 'EN_PROCESO';

    const putRes = await fetch(`${API}/incidencias/${incidenciaId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(inc)
    });

    if (putRes.ok) {
      const updatedInc = await putRes.json();
      
      // En lugar de recargar toda la lista, actualizamos el DOM de la incidencia actual
      if (state.incidenciaActiva && state.incidenciaActiva.incidenciaId === updatedInc.incidenciaId) {
        state.incidenciaActiva = updatedInc;
      }
      
      const liElement = document.querySelector(`li[data-id="${incidenciaId}"]`);
      if (liElement) {
        // Actualizar datos del elemento
        liElement._incData = updatedInc;
        
        // Actualizar etiqueta de estado
        const estadoTag = liElement.querySelector('.estado-tag');
        if (estadoTag) estadoTag.textContent = updatedInc.estado;

        // Cambiar botón de Atender a Finalizar
        const btnAtender = liElement.querySelector('.btn-atender-incidencia');
        if (btnAtender) {
          const newBtn = document.createElement('button');
          newBtn.className = 'btn-cerrar-incidencia';
          newBtn.style.cssText = 'background: var(--error-bg); color: var(--error-text); border: 1px solid var(--error-text);';
          newBtn.textContent = 'Finalizar Incidencia';
          newBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            cerrarIncidencia(updatedInc.incidenciaId);
          });
          btnAtender.replaceWith(newBtn);
        }

        // Si el chat de notas está abierto, actualizar su estado para mostrar formulario
        const statusMsg = liElement.querySelector('.chat-status-msg');
        const form = liElement.querySelector('.chat-form');
        if (statusMsg && form && updatedInc.empleadoId) {
          statusMsg.classList.add('hidden');
          form.classList.remove('hidden');
        }

        if (!silencioso) {
          showNotification('Incidencia atendida correctamente');
          // Solo abrimos las notas si no estaban abiertas
          const existingNotes = liElement.querySelector('.notes-container');
          if (!existingNotes) {
            abrirNotas(updatedInc, liElement);
          }
        }
      } else {
        // Fallback si no se encuentra el elemento en el DOM
        refreshIncidencias();
        if (!silencioso) {
          showNotification('Incidencia atendida correctamente');
          setTimeout(() => {
            const newLi = document.querySelector(`li[data-id="${incidenciaId}"]`);
            if (newLi) abrirNotas(updatedInc, newLi);
          }, 500);
        }
      }
    } else {
      const errorText = await putRes.text();
      if (!silencioso) showNotification('Error al atender la incidencia: ' + errorText);
    }
  } catch (err) {
    if (!silencioso) showNotification(err.message);
  }
}

async function cambiarPrioridad(incidenciaId, nuevaPrioridad) {
  try {
    const res = await fetch(`${API}/incidencias/${incidenciaId}`);
    if (!res.ok) throw new Error('No se pudo obtener la incidencia');
    const inc = await res.json();

    inc.prioridad = nuevaPrioridad;

    const putRes = await fetch(`${API}/incidencias/${incidenciaId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(inc)
    });

    if (putRes.ok) {
      onLogged(state.usuario);
    } else {
      const errorText = await putRes.text();
      showNotification('Error al cambiar la prioridad: ' + errorText);
    }
  } catch (err) {
    showNotification(err.message);
  }
}

// Función para marcar una incidencia como cerrada
async function cerrarIncidencia(incidenciaId) {
  if (!confirm('¿Estás seguro de que deseas cerrar esta incidencia? Se moverá al historial.')) return; // Confirmación de seguridad

  try {
    const res = await fetch(`${API}/incidencias/${incidenciaId}`); // Obtiene datos actuales
    if (!res.ok) throw new Error('No se pudo obtener la incidencia');
    const inc = await res.json();

    inc.estado = 'CERRADA'; // Cambia el estado a Cerrada

    const putRes = await fetch(`${API}/incidencias/${incidenciaId}`, { // Actualiza en servidor
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(inc)
    });

    if (putRes.ok) { // Si se cerró correctamente
      showNotification('Incidencia cerrada y movida al historial');
      onLogged(state.usuario); // Refresca las listas
      cerrarNotasActuales(); // Cierra el panel de notas si estuviera abierto
    } else {
      showNotification('Error al cerrar la incidencia');
    }
  } catch (err) {
    console.error(err);
    showNotification(err.message);
  }
}

// --- GESTIÓN DE USUARIOS (ADMIN) ---

async function fetchUsuarios() {
  try {
    const res = await fetch(`${API}/clientes`);
    if (!res.ok) throw new Error('Error al cargar usuarios');
    state.usuarios = await res.json();
    renderUsuarios();
  } catch (err) {
    console.error(err);
  }
}

function renderUsuarios(filtro = '') {
  const tbody = q('#usuarios-body');
  if (!tbody) return;
  tbody.innerHTML = '';

  const usuariosFiltrados = state.usuarios.filter(u => {
    const nombreCompleto = `${u.nombre} ${u.apellido}`.toLowerCase();
    return nombreCompleto.includes(filtro.toLowerCase()) || u.correo.toLowerCase().includes(filtro.toLowerCase());
  });

  usuariosFiltrados.forEach(u => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td style="padding: 10px;">${u.nombre} ${u.apellido}</td>
      <td style="padding: 10px;">${u.correo}</td>
      <td style="padding: 10px;"><span class="badge badge-media">${u.rol}</span></td>
      <td style="padding: 10px;">
        <span style="color: ${u.activo ? '#166534' : '#991b1b'}; font-weight: bold;">
          ${u.activo ? 'ACTIVO' : 'INACTIVO'}
        </span>
      </td>
      <td style="padding: 10px;">
        <button class="btn-edit-user" data-id="${u.clienteId}" style="padding: 5px 10px; font-size: 0.8rem;">Editar</button>
        <button class="btn-toggle-user" data-id="${u.clienteId}" style="padding: 5px 10px; font-size: 0.8rem; background: ${u.activo ? '#991b1b' : '#166534'}">
          ${u.activo ? 'Desactivar' : 'Activar'}
        </button>
      </td>
    `;
    tbody.appendChild(tr);
  });

  // Eventos para botones de la tabla
  tbody.querySelectorAll('.btn-edit-user').forEach(btn => {
    btn.addEventListener('click', () => abrirFormUsuario(btn.dataset.id));
  });
  tbody.querySelectorAll('.btn-toggle-user').forEach(btn => {
    btn.addEventListener('click', () => toggleUsuarioActivo(btn.dataset.id));
  });
}

function abrirFormUsuario(id = null) {
  const formView = q('#usuario-form-view');
  const title = q('#usuario-form-title');
  const form = q('#usuario-form');
  const passHelp = q('#pass-help');
  const labelPass = q('#label-user-pass');
  
  form.reset();
  q('#user-id').value = id || '';
  q('#user-msg').textContent = '';
  show('#usuario-form-view');
  hide('#btn-nuevo-usuario');

  if (id) {
    title.textContent = 'Editar Usuario';
    const u = state.usuarios.find(user => user.clienteId == id);
    if (u) {
      q('#user-nombre').value = u.nombre;
      q('#user-apellido').value = u.apellido;
      q('#user-email').value = u.correo;
      q('#user-rol').value = u.rol;
      q('#user-activo').value = String(u.activo);
      labelPass.textContent = 'Cambiar Contraseña';
      show('#pass-help');
      q('#user-pass').required = false;
    }
  } else {
    title.textContent = 'Nuevo Usuario';
    labelPass.textContent = 'Contraseña';
    hide('#pass-help');
    q('#user-pass').required = true;
  }
}

async function toggleUsuarioActivo(id) {
  const u = state.usuarios.find(user => user.clienteId == id);
  if (!u) return;
  
  const confirmMsg = u.activo 
    ? `¿Estás seguro de que deseas desactivar a ${u.nombre}? No podrá iniciar sesión.`
    : `¿Deseas activar a ${u.nombre}?`;
    
  if (!confirm(confirmMsg)) return; // Se mantiene confirm() por ser una acción crítica de seguridad

  const data = { ...u, activo: !u.activo };
  try {
    const res = await fetch(`${API}/clientes/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    if (res.ok) {
      showNotification('Estado del usuario actualizado');
      fetchUsuarios();
    } else {
      showNotification('Error al cambiar estado del usuario');
    }
  } catch (err) {
    console.error(err);
  }
}

// Función para abrir la vista de notas de una incidencia concreta (ahora embebida)
async function abrirNotas(incidencia, elemento = null) {
  if (!elemento) return;

  // Si ya estaba abierta en este mismo elemento, no hacemos nada (o podríamos cerrarla)
  const existingNotes = elemento.querySelector('.notes-container');
  if (existingNotes) {
    cerrarNotasActuales();
    return;
  }

  // Primero cerramos cualquier otra nota abierta para evitar duplicados
  cerrarNotasActuales();

  state.incidenciaActiva = incidencia; // Establece la incidencia en el estado global

  // Clonamos la plantilla de notas
  const template = q('#notes-template').firstElementChild.cloneNode(true);

  // Configuramos el formulario y eventos de la nueva instancia
  const form = template.querySelector('.chat-form');
  const input = template.querySelector('.chat-input');
  const messagesContainer = template.querySelector('.chat-messages');
  const closeBtn = template.querySelector('.btn-cerrar-notas');
  const statusMsg = template.querySelector('.chat-status-msg');

  // Evento para cerrar
  closeBtn.addEventListener('click', (e) => {
    e.stopPropagation();
    cerrarNotasActuales();
  });

  // Evento para enviar nota
  form.addEventListener('submit', (e) => {
    e.preventDefault();
    const contenido = input.value.trim();
    if (contenido) {
      añadirNota(contenido, messagesContainer, input);
    }
  });

  // Mostramos/ocultamos formulario según el rol y si está asignada
  // Los Clientes siempre pueden añadir notas (sus propias dudas)
  // Los Empleados/Admin solo si la incidencia está asignada (a ellos u otro)
  if (state.usuario.rol === 'CLIENTE') {
    statusMsg.classList.add('hidden');
    form.classList.remove('hidden');
  } else {
    // Para Empleados/Admin, solo mostramos el formulario si la incidencia está atendida
    if (incidencia.empleadoId) {
      statusMsg.classList.add('hidden');
      form.classList.remove('hidden');
    } else {
      statusMsg.innerText = 'Debes atender la incidencia para poder añadir notas.';
      statusMsg.classList.remove('hidden');
      form.classList.add('hidden');
    }
  }

  // Guardamos la incidencia en el elemento para recuperarla fácilmente
  elemento._incData = incidencia;

  // Inyectamos la plantilla en el elemento de la incidencia
  elemento.appendChild(template);

  cargarNotas(messagesContainer); // Carga las notas inmediatamente en el nuevo contenedor

  if (state.notasInterval) clearInterval(state.notasInterval); // Limpia intervalos previos
  state.notasInterval = setInterval(() => cargarNotas(messagesContainer), 10000);

  elemento.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

// Función para cerrar las notas que estén abiertas actualmente
function cerrarNotasActuales() {
  const allContainers = document.querySelectorAll('.notes-container');
  allContainers.forEach(c => {
    // No eliminar la plantilla original
    if (!c.closest('#notes-template')) {
      c.remove();
    }
  });
  state.incidenciaActiva = null;
  if (state.notasInterval) clearInterval(state.notasInterval);
}

// Función para descargar y renderizar las notas de la incidencia activa
async function cargarNotas(container = null) {
  if (!state.incidenciaActiva) return;

  // Si no se pasa contenedor, intentamos buscarlo (por seguridad en el polling)
  if (!container) {
    container = document.querySelector(`li[data-id="${state.incidenciaActiva.incidenciaId}"] .chat-messages, tr[data-id="${state.incidenciaActiva.incidenciaId}"] .chat-messages`);
  }
  if (!container) return;

  try {
    const res = await fetch(`${API}/notas/incidencia/${state.incidenciaActiva.incidenciaId}`);
    if (!res.ok) return;
    const notas = await res.json();

    const isAtBottom = container.scrollHeight - container.scrollTop <= container.clientHeight + 100;

    container.innerHTML = '';
    if (notas.length === 0) {
      container.innerHTML = '<div style="text-align:center; color:var(--text-muted); padding:20px;">No hay notas en esta incidencia.</div>';
    }

    notas.forEach(nota => {
      const card = document.createElement('div');
      card.className = 'nota-card';

      const isMe = (nota.autor === state.usuario.rol);
      if (isMe) {
        card.style.borderLeft = '4px solid var(--primary)';
      } else {
        card.style.borderLeft = '4px solid #94a3b8';
      }

      card.innerHTML = `
        <div class="nota-header">
          <span class="nota-autor">${nota.nombreAutor || nota.autor}</span>
          <span class="nota-fecha">${new Date(nota.fechaCreacion).toLocaleString()}</span>
        </div>
        <div class="nota-contenido">${nota.contenido}</div>
      `;
      container.appendChild(card);
    });

    if (isAtBottom) {
      container.scrollTo({ top: container.scrollHeight, behavior: 'smooth' });
    }
  } catch (err) {
    console.error('Error cargando notas:', err);
  }
}

// Función para enviar una nueva nota al servidor
async function añadirNota(contenido, container, input) {
  if (!state.incidenciaActiva) return;

  const data = {
    incidenciaId: state.incidenciaActiva.incidenciaId,
    autor: state.usuario.rol,
    nombreAutor: `${state.usuario.nombre} ${state.usuario.apellido}`,
    contenido: contenido,
    fechaCreacion: new Date().toISOString()
  };

  try {
    const res = await fetch(`${API}/notas`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    if (res.ok) {
      input.value = '';
      cargarNotas(container);
      
      // Si un empleado o admin deja una nota en una incidencia sin asignar, la atiende automáticamente
      if ((state.usuario.rol === 'EMPLEADO' || state.usuario.rol === 'ADMINISTRADOR') && !state.incidenciaActiva.empleadoId) {
        // Usamos modo silencioso para que no se cierre el chat ni salte el mensaje de éxito de "atender"
        atenderIncidencia(state.incidenciaActiva.incidenciaId, true);
      }
    }
  } catch (err) {
    console.error('Error añadiendo nota:', err);
  }
}

// Función para registrar un nuevo cliente en el sistema
async function registrarCliente(data) {
  const res = await fetch(`${API}/clientes`, { // Petición POST al endpoint de registro
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });

  if (res.status === 409) { // Conflicto: el email ya existe
    const errorMsg = await res.text();
    throw new Error(errorMsg);
  }

  if (!res.ok) throw new Error('No se pudo registrar el cliente');
  return res.json(); // Devuelve el usuario recién creado
}

// Función para crear una nueva incidencia (solo Clientes)
async function crearIncidencia(data) {
  const res = await fetch(`${API}/incidencias`, { // Petición POST de creación
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error('No se pudo crear la incidencia');
  return res.json(); // Devuelve la incidencia creada
}

// Genérica para obtener e inyectar datos
async function fetchIncidencias(url, listId, historyBodyId) {
  console.log(`[DEBUG] Fetching incidencias from: ${url}`);
  const ul = q(listId);
  const tbodyHist = historyBodyId ? q(historyBodyId) : null;

  if (!ul) {
    console.error(`[DEBUG] Container ${listId} not found`);
    return;
  }

  ul.innerHTML = '<li>Cargando incidencias...</li>';
  if (tbodyHist) tbodyHist.innerHTML = '<tr><td colspan="5">Cargando historial...</td></tr>';

  try {
    const res = await fetch(url);
    if (!res.ok) throw new Error(`HTTP ${res.status}: Error al cargar incidencias`);
    let data = await res.json();
    console.log(`[DEBUG] Data received:`, data);

    const allIncidencias = Array.isArray(data) ? data : (data.incidencias || []);

    let activas = allIncidencias.filter(i => i.estado !== 'CERRADA'); // Filtra las que no están cerradas
    const cerradas = allIncidencias.filter(i => i.estado === 'CERRADA'); // Filtra las cerradas (historial)

    // Filtrar para Empleados: Solo ver incidencias sin asignar O asignadas a ellos mismos
    if (state.usuario && state.usuario.rol === 'EMPLEADO') {
      activas = activas.filter(i => !i.empleadoId || i.empleadoId === state.usuario.clienteId);
    }

    renderListaIncidencias(activas, ul);
    if (tbodyHist) renderHistorialTabla(cerradas, tbodyHist);

  } catch (error) {
    console.error('[DEBUG] Fetch error:', error);
    ul.innerHTML = `<li>Error: ${error.message}</li>`;
  }
}


function renderListaIncidencias(incidencias, container) {
  container.innerHTML = '';
  if (incidencias.length === 0) {
    container.innerHTML = '<li>No hay incidencias.</li>';
    return;
  }

  incidencias.forEach(inc => {
    const li = document.createElement('li');
    li.setAttribute('data-id', inc.incidenciaId);
    li._incData = inc; // Guardamos el objeto de la incidencia directamente en el nodo

    let botones = `<button class="btn-ver-notas" style="background: var(--bg); color: var(--primary); border: 1px solid var(--primary); margin-right: 8px;">Ver Notas</button>`;

    if (state.usuario.rol === 'CLIENTE') {
      botones += `<button class="btn-cerrar-incidencia" style="background: var(--error-bg); color: var(--error-text); border: 1px solid var(--error-text);">Cerrar Incidencia</button>`;
    } else if (state.usuario.rol === 'EMPLEADO' || state.usuario.rol === 'ADMINISTRADOR') {
      if (!inc.empleadoId) {
        botones += `<button class="btn-atender-incidencia">Atender Incidencia</button>`;
      } else {
        botones += `<button class="btn-cerrar-incidencia" style="background: var(--error-bg); color: var(--error-text); border: 1px solid var(--error-text);">Finalizar Incidencia</button>`;
      }
    }

    const pClass = `badge badge-${(inc.prioridad || 'MEDIA').toLowerCase()}`;
    let prioridadHtml = `<span class="${pClass}">${inc.prioridad}</span>`;

    li.innerHTML = `
      <div class="inc-header">
        <div>
          ${prioridadHtml}
          <span class="estado-tag" style="margin-left: 10px;">${inc.estado || 'PENDIENTE'}</span>
        </div>
        <small style="color: var(--text-muted)">${new Date(inc.fechaCreacion).toLocaleDateString()}</small>
      </div>
      <h4 style="margin: 10px 0 5px 0;">${inc.titulo}</h4>
      <p style="font-size: 0.9rem; color: var(--text-muted); margin-bottom: 15px;">${inc.descripcion}</p>
      <div style="font-size: 0.85rem; margin-bottom: 10px;">
        <strong>Cliente:</strong> ${inc.nombreCliente || 'Anonimo'}
      </div>
      <div class="actions-row">
        ${botones}
      </div>
    `;

    // Asignamos eventos de forma segura
    li.querySelector('.btn-ver-notas').addEventListener('click', (e) => {
      e.stopPropagation();
      abrirNotas(li._incData, li);
    });

    const btnCerrar = li.querySelector('.btn-cerrar-incidencia');
    if (btnCerrar) {
      btnCerrar.addEventListener('click', (e) => {
        e.stopPropagation();
        cerrarIncidencia(inc.incidenciaId);
      });
    }

    const btnAtender = li.querySelector('.btn-atender-incidencia');
    if (btnAtender) {
      btnAtender.addEventListener('click', (e) => {
        e.stopPropagation();
        atenderIncidencia(inc.incidenciaId);
      });
    }

    container.appendChild(li);
  });
}

// Función para dibujar las filas del historial (incidencias cerradas)
function renderHistorialTabla(incidencias, tbody) {
  tbody.innerHTML = '';
  if (incidencias.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding:20px; color:var(--text-muted);">No hay incidencias cerradas en el historial.</td></tr>';
    return;
  }

  incidencias.forEach(inc => { // Crea una fila <tr> por cada incidencia cerrada
    const tr = document.createElement('tr');
    tr.style.borderBottom = '1px solid #f3f4f6';
    tr.setAttribute('data-id', inc.incidenciaId);
    tr._incData = inc;

    tr.innerHTML = `
      <td style="padding: 10px;">${inc.titulo}</td>
      <td style="padding: 10px;">${inc.nombreCliente || 'Desconocido'}</td>
      <td style="padding: 10px;"><span class="badge badge-${(inc.prioridad || 'MEDIA').toLowerCase()}">${inc.prioridad}</span></td>
      <td style="padding: 10px;">${new Date(inc.fechaCreacion).toLocaleDateString()}</td>
      <td style="padding: 10px;">
        <button class="btn-ver-notas" style="background: var(--bg); color: var(--primary); border: 1px solid var(--primary); padding: 4px 8px; font-size: 0.8rem;">Ver Notas</button>
      </td>
    `;

    tr.querySelector('.btn-ver-notas').addEventListener('click', (e) => {
      e.stopPropagation();
      abrirNotas(tr._incData, tr);
    });

    tbody.appendChild(tr);
  });
}

// Función exclusiva del Administrador para cargar la lista de todos los usuarios
async function cargarUsuariosAdmin() {
  const tbody = q('#usuarios-body');
  tbody.innerHTML = '<tr><td colspan="4">Cargando...</td></tr>';
  try {
    const res = await fetch(`${API}/clientes`); // Obtiene todos los usuarios
    if (!res.ok) throw new Error('No se pudieron cargar los usuarios');
    const usuarios = await res.json();

    tbody.innerHTML = '';
    usuarios.forEach(user => {
      const tr = document.createElement('tr');
      tr.style.borderBottom = '1px solid #f3f4f6';

      // Lógica para el botón de cambio de rol dinámico
      const actions = user.rol === 'CLIENTE'
        ? `<button onclick="cambiarRol(${user.clienteId}, 'EMPLEADO')">Hacer Empleado</button>`
        : user.rol === 'EMPLEADO'
          ? `<button onclick="cambiarRol(${user.clienteId}, 'CLIENTE')">Hacer Cliente</button>`
          : '<em>Admin</em>';

      tr.innerHTML = `
        <td style="padding: 10px;">${user.nombre} ${user.apellido}</td>
        <td style="padding: 10px;">${user.correo}</td>
        <td style="padding: 10px;"><span style="background: #e5e7eb; padding: 2px 6px; border-radius: 4px; font-size: 0.8em;">${user.rol}</span></td>
        <td style="padding: 10px;">${actions}</td>
      `;
      tbody.appendChild(tr);
    });
  } catch (error) {
    tbody.innerHTML = `<tr><td colspan="4">Error: ${error.message}</td></tr>`;
  }
}

// Función para cambiar el rol de un usuario (Admin solamente)
async function cambiarRol(id, nuevoRol) {
  try {
    const userRes = await fetch(`${API}/clientes/${id}`); // Obtiene los datos del usuario actual
    const user = await userRes.json();
    user.rol = nuevoRol; // Cambia solo el campo rol

    const res = await fetch(`${API}/clientes/${id}`, { // Actualiza en el servidor
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(user)
    });

    if (res.ok) {
      cargarUsuariosAdmin(); // Recarga la tabla de usuarios para reflejar el cambio
    } else {
      alert('Error al cambiar el rol');
    }
  } catch (err) {
    console.error(err);
    alert('Error de conexión');
  }
}

// Función que refresca las incidencias según el rol sin resetear la vista completa
function refreshIncidencias() {
  if (!state.usuario) return;
  if (state.usuario.rol === 'ADMINISTRADOR') {
    fetchIncidencias(`${API}/incidencias`, '#admin-incidencias-list', '#admin-historial-incidencias-body');
  } else if (state.usuario.rol === 'EMPLEADO') {
    fetchIncidencias(`${API}/incidencias`, '#todas-incidencias-list');
  } else {
    fetchIncidencias(`${API}/incidencias/cliente/${state.usuario.clienteId}`, '#incidencias-list');
  }
}

// Vinculación de funciones al objeto window para poder llamarlas desde el HTML (onclick)
window.cambiarRol = cambiarRol;
window.atenderIncidencia = atenderIncidencia;
window.cerrarIncidencia = cerrarIncidencia;
window.abrirNotas = abrirNotas;
window.cerrarNotasActuales = cerrarNotasActuales;
window.fetchIncidencias = fetchIncidencias; // Añadido para debugging si se necesita llamar desde consola

// Función que se ejecuta cuando el usuario se loguea con éxito
function onLogged(usuario) {
  state.usuario = usuario; // Guarda el usuario en el estado global
  localStorage.setItem('tfg_usuario', JSON.stringify(usuario)); // Persiste la sesión
  q('#welcome').textContent = `Bienvenido/a, ${usuario.nombre} ${usuario.apellido}`; // Saludo personalizado
  q('#role-info').textContent = `Rol: ${usuario.rol}`; // Muestra el rol actual

  hide('#auth-view'); // Oculta login
  hide('#register-view'); // Oculta registro
  show('#app-view'); // Muestra el panel principal

  // Oculta todas las sub-vistas de roles primero
  document.querySelectorAll('.role-view').forEach(v => v.classList.add('hidden'));

  // Activa la vista correspondiente según el rol del usuario
  if (usuario.rol === 'ADMINISTRADOR') {
    show('#admin-view');
    fetchUsuarios(); // Carga tabla de usuarios
    fetchIncidencias(`${API}/incidencias`, '#admin-incidencias-list', '#admin-historial-incidencias-body'); // Carga todas las incidencias
  } else if (usuario.rol === 'EMPLEADO') {
    show('#empleado-view');
    fetchIncidencias(`${API}/incidencias`, '#todas-incidencias-list'); // Carga todas las incidencias
  } else { // Rol CLIENTE
    show('#cliente-view');
    fetchIncidencias(`${API}/incidencias/cliente/${usuario.clienteId}`, '#incidencias-list'); // Carga solo sus incidencias
  }
}

// Evento para cambiar de vista de login a registro
q('#btn-to-register').addEventListener('click', () => {
  hide('#auth-view');
  show('#register-view');
  q('#login-msg').textContent = '';
});

// Evento para procesar el formulario de inicio de sesión
q('#login-form').addEventListener('submit', async (e) => {
  e.preventDefault(); // Evita que la página se recargue
  if (!validateForm(e.target)) return;
  const email = q('#login-email').value.trim();
  const pass = q('#login-pass').value.trim();
  q('#login-msg').textContent = '';

  try {
    const usuario = await login(email, pass); // Llama a la función de login
    if (usuario) {
      onLogged(usuario); // Inicializa la aplicación si es correcto
    } else {
      q('#login-msg').textContent = 'Credenciales incorrectas o la cuenta no existe.';
    }
  } catch (err) {
    q('#login-msg').textContent = err.message;
  }
});

// Evento para procesar el formulario de registro de cliente
q('#register-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  if (!validateForm(e.target)) return;
  const data = {
    nombre: q('#reg-nombre').value.trim(),
    apellido: q('#reg-apellido').value.trim(),
    correo: q('#reg-email').value.trim(),
    contrasena: q('#reg-pass').value.trim(), // Contraseña en texto plano
    activo: true,
    rol: 'CLIENTE' // Por defecto se registran como clientes
  };
  q('#reg-msg').textContent = '';
  try {
    const usuario = await registrarCliente(data); // Llama a la función de registro
    onLogged(usuario); // Loguea automáticamente tras registrarse
  } catch (err) {
    q('#reg-msg').textContent = err.message;
  }
});

// Evento para volver al login desde el registro
q('#reg-cancel').addEventListener('click', () => {
  hide('#register-view');
  show('#auth-view');
});

// Evento para cerrar la sesión actual
q('#logout').addEventListener('click', () => {
  state.usuario = null;
  localStorage.removeItem('tfg_usuario'); // Elimina la sesión persistida
  cerrarNotasActuales();
  hide('#app-view');
  show('#auth-view');
  q('#login-form').reset(); // Limpia campos de login
  q('#register-form').reset(); // Limpia campos de registro
  document.querySelectorAll('.create-incidencia-view').forEach(v => v.classList.add('hidden')); // Cierra formulario de creación si estaba abierto
  document.querySelectorAll('.btn-nueva-incidencia').forEach(b => b.style.display = 'block'); // Vuelve a mostrar el botón "Crear"
});


// Eventos para el sistema de pestañas de Administrador
document.querySelectorAll('.tab-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    cerrarNotasActuales();
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active')); // Quita estilo activo
    document.querySelectorAll('.tab-content').forEach(c => c.classList.add('hidden')); // Oculta todos los contenidos
    btn.classList.add('active'); // Activa la pestaña clickeada
    show(`#${btn.dataset.tab}`); // Muestra el contenido asociado (tab-usuarios o tab-todas-incidencias)
  });
});

// Evento para mostrar el formulario de creación de incidencia
document.querySelectorAll('.btn-nueva-incidencia').forEach(btn => {
  btn.addEventListener('click', (e) => {
    const view = e.target.closest('.role-view');
    view.querySelector('.create-incidencia-view').classList.remove('hidden'); // Muestra el formulario
    e.target.style.display = 'none'; // Oculta el botón de "Crear" para no duplicar
    const form = view.querySelector('.incidencia-form');
    form.reset();
    view.querySelector('.inc-msg').textContent = '';
  });
});

// Evento para cancelar/ocultar el formulario de creación de incidencia
document.querySelectorAll('.inc-cancel').forEach(btn => {
  btn.addEventListener('click', (e) => {
    const view = e.target.closest('.role-view');
    view.querySelector('.create-incidencia-view').classList.add('hidden'); // Oculta formulario
    view.querySelector('.btn-nueva-incidencia').style.display = 'block'; // Muestra de nuevo el botón "Crear"
  });
});

// Función para enviar la nueva incidencia al servidor
document.querySelectorAll('.incidencia-form').forEach(form => {
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    if (!validateForm(e.target)) return;
    const view = e.target.closest('.role-view');
    const data = {
      clienteId: state.usuario.clienteId,
      titulo: e.target.querySelector('.inc-titulo').value.trim(),
      descripcion: e.target.querySelector('.inc-desc').value.trim(),
      prioridad: e.target.querySelector('.inc-prioridad').value,
      estado: 'PENDIENTE',
      fechaCreacion: new Date().toISOString()
    };

    const msgDiv = view.querySelector('.inc-msg');
    msgDiv.textContent = '';
    try {
      await crearIncidencia(data); // Petición de creación
      view.querySelector('.create-incidencia-view').classList.add('hidden'); // Oculta formulario
      view.querySelector('.btn-nueva-incidencia').style.display = 'block'; // Muestra botón "Crear"

      if (state.usuario.rol === 'CLIENTE') { // Si es cliente, refresca su lista personal
        fetchIncidencias(`${API}/incidencias/cliente/${state.usuario.clienteId}`, '#incidencias-list', '#historial-incidencias-list');
      }
    } catch (err) {
      msgDiv.textContent = err.message; // Muestra error si falla
    }
  });
});

// Eventos para el formulario de gestión de usuarios (Admin)
q('#search-usuarios').addEventListener('input', (e) => {
  renderUsuarios(e.target.value);
});

// Evento para actualizar visualmente el rol según el email en el formulario
q('#user-email').addEventListener('input', (e) => {
  const email = e.target.value.trim().toLowerCase();
  const rolSelect = q('#user-rol');
  if (email.endsWith('@incidencias.com')) {
    rolSelect.value = 'EMPLEADO';
  } else {
    rolSelect.value = 'CLIENTE';
  }
});
q('#btn-nuevo-usuario').addEventListener('click', () => abrirFormUsuario());
q('#user-cancel').addEventListener('click', () => {
  hide('#usuario-form-view');
  show('#btn-nuevo-usuario');
});

q('#usuario-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  if (!validateForm(e.target)) return;
  const id = q('#user-id').value;
  const msgDiv = q('#user-msg');
  msgDiv.textContent = '';

  const email = q('#user-email').value.trim();
  const data = {
    nombre: q('#user-nombre').value.trim(),
    apellido: q('#user-apellido').value.trim(),
    correo: email,
    activo: q('#user-activo').value === 'true'
  };

  const pass = q('#user-pass').value.trim();
  if (pass) data.contrasena = pass;

  try {
    const url = id ? `${API}/clientes/${id}` : `${API}/clientes`;
    const method = id ? 'PUT' : 'POST';

    const res = await fetch(url, {
      method: method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });

    if (res.ok) {
      hide('#usuario-form-view');
      show('#btn-nuevo-usuario');
      fetchUsuarios();
    } else {
      const errorText = await res.text();
      msgDiv.textContent = 'Error: ' + errorText;
    }
  } catch (err) {
    msgDiv.textContent = 'Error de conexión';
  }
});

// Restaurar sesión al cargar la página
document.addEventListener('DOMContentLoaded', () => {
  const savedUser = localStorage.getItem('tfg_usuario');
  if (savedUser) {
    try {
      const usuario = JSON.parse(savedUser);
      onLogged(usuario);
    } catch (e) {
      localStorage.removeItem('tfg_usuario');
    }
  }
});