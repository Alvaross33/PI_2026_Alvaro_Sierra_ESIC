const API = '/api';
const q = (selector) => document.querySelector(selector);
const show = (id) => q(id).classList.remove('hidden');
const hide = (id) => q(id).classList.add('hidden');

const state = {
  usuario: null,
  incidenciaActiva: null,
  notasInterval: null,
  usuarios: []
};

function showNotification(message, duration = 3000) {
  const container = q('#custom-alert-container');
  container.textContent = message;
  container.classList.remove('hidden');
  setTimeout(() => container.classList.add('hidden'), duration);
}

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
  if (!isValid) showNotification('Por favor, rellena todos los campos obligatorios');
  return isValid;
}

async function login(email, pass) {
  const res = await fetch(`${API}/clientes/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ correo: email, contrasena: pass }),
  });
  if (res.ok) return res.json();
  if (res.status === 403) throw new Error(await res.text());
  if (res.status === 404 || res.status === 401) return null;
  throw new Error('Error en el servidor');
}

async function registrarCliente(data) {
  const res = await fetch(`${API}/clientes`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });
  if (res.status === 409) throw new Error(await res.text());
  if (!res.ok) throw new Error('No se pudo registrar el cliente');
  return res.json();
}

async function crearIncidencia(data) {
  const res = await fetch(`${API}/incidencias`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error('No se pudo crear la incidencia');
  return res.json();
}

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

    if (!putRes.ok) {
      if (!silencioso) showNotification('Error al atender la incidencia: ' + await putRes.text());
      return;
    }

    const updatedInc = await putRes.json();
    if (state.incidenciaActiva && state.incidenciaActiva.incidenciaId === updatedInc.incidenciaId) {
      state.incidenciaActiva = updatedInc;
    }

    const liElement = document.querySelector(`li[data-id="${incidenciaId}"]`);
    if (liElement) {
      liElement._incData = updatedInc;
      const estadoTag = liElement.querySelector('.estado-tag');
      if (estadoTag) estadoTag.textContent = updatedInc.estado;

      const btnAtender = liElement.querySelector('.btn-atender-incidencia');
      if (btnAtender) {
        const newBtn = document.createElement('button');
        newBtn.className = 'btn-cerrar-incidencia';
        newBtn.textContent = 'Finalizar Incidencia';
        newBtn.addEventListener('click', (e) => {
          e.stopPropagation();
          cerrarIncidencia(updatedInc.incidenciaId);
        });
        btnAtender.replaceWith(newBtn);
      }

      const statusMsg = liElement.querySelector('.chat-status-msg');
      const form = liElement.querySelector('.chat-form');
      if (statusMsg && form && updatedInc.empleadoId) {
        statusMsg.classList.add('hidden');
        form.classList.remove('hidden');
      }

      if (!silencioso) {
        showNotification('Incidencia atendida correctamente');
        if (!liElement.querySelector('.notes-container')) abrirNotas(updatedInc, liElement);
      }
    } else {
      refreshIncidencias();
      if (!silencioso) {
        showNotification('Incidencia atendida correctamente');
        setTimeout(() => {
          const newLi = document.querySelector(`li[data-id="${incidenciaId}"]`);
          if (newLi) abrirNotas(updatedInc, newLi);
        }, 500);
      }
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
    if (putRes.ok) onLogged(state.usuario);
    else showNotification('Error al cambiar la prioridad: ' + await putRes.text());
  } catch (err) {
    showNotification(err.message);
  }
}

async function cerrarIncidencia(incidenciaId) {
  if (!confirm('¿Estás seguro de que deseas cerrar esta incidencia? Se moverá al historial.')) return;
  try {
    const res = await fetch(`${API}/incidencias/${incidenciaId}`);
    if (!res.ok) throw new Error('No se pudo obtener la incidencia');
    const inc = await res.json();
    inc.estado = 'CERRADA';
    const putRes = await fetch(`${API}/incidencias/${incidenciaId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(inc)
    });
    if (putRes.ok) {
      showNotification('Incidencia cerrada y movida al historial');
      onLogged(state.usuario);
      cerrarNotasActuales();
    } else showNotification('Error al cerrar la incidencia');
  } catch (err) {
    showNotification(err.message);
  }
}

async function fetchIncidencias(url, listId, historyBodyId) {
  const ul = q(listId);
  const tbodyHist = historyBodyId ? q(historyBodyId) : null;
  if (!ul) return;
  ul.innerHTML = '<li>Cargando incidencias...</li>';
  if (tbodyHist) tbodyHist.innerHTML = '<tr><td colspan="5">Cargando historial...</td></tr>';
  
  try {
    const res = await fetch(url);
    if (!res.ok) throw new Error(`HTTP ${res.status}: Error al cargar incidencias`);
    const data = await res.json();
    const allIncidencias = Array.isArray(data) ? data : (data.incidencias || []);
    let activas = allIncidencias.filter(i => i.estado !== 'CERRADA');
    const cerradas = allIncidencias.filter(i => i.estado === 'CERRADA');
    if (state.usuario && state.usuario.rol === 'EMPLEADO') {
      activas = activas.filter(i => !i.empleadoId || i.empleadoId === state.usuario.clienteId);
    }
    renderListaIncidencias(activas, ul);
    if (tbodyHist) renderHistorialTabla(cerradas, tbodyHist);
  } catch (error) {
    ul.innerHTML = `<li>Error: ${error.message}</li>`;
  }
}

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

function renderListaIncidencias(incidencias, container) {
  container.innerHTML = '';
  if (incidencias.length === 0) {
    container.innerHTML = '<li>No hay incidencias.</li>';
    return;
  }
  incidencias.forEach(inc => {
    const li = document.createElement('li');
    li.setAttribute('data-id', inc.incidenciaId);
    li._incData = inc;
    let botones = `<button class="btn-ver-notas">Ver Notas</button>`;
    if (state.usuario.rol === 'CLIENTE') {
      botones += `<button class="btn-cerrar-incidencia">Cerrar Incidencia</button>`;
    } else if (state.usuario.rol === 'EMPLEADO' || state.usuario.rol === 'ADMINISTRADOR') {
      if (!inc.empleadoId) botones += `<button class="btn-atender-incidencia">Atender Incidencia</button>`;
      else botones += `<button class="btn-cerrar-incidencia">Finalizar Incidencia</button>`;
    }
    const pClass = `badge badge-${(inc.prioridad || 'MEDIA').toLowerCase()}`;
    li.innerHTML = `
      <div class="inc-header">
        <div><span class="${pClass}">${inc.prioridad}</span><span class="estado-tag">${inc.estado || 'PENDIENTE'}</span></div>
        <small class="text-muted">${new Date(inc.fechaCreacion).toLocaleDateString()}</small>
      </div>
      <h4 style="margin: 10px 0 5px 0;">${inc.titulo}</h4>
      <p class="text-desc">${inc.descripcion}</p>
      <div class="text-client"><strong>Cliente:</strong> ${inc.nombreCliente || 'Anonimo'}</div>
      <div class="actions-row">${botones}</div>
    `;
    li.querySelector('.btn-ver-notas').addEventListener('click', (e) => { e.stopPropagation(); abrirNotas(li._incData, li); });
    const btnCerrar = li.querySelector('.btn-cerrar-incidencia');
    if (btnCerrar) btnCerrar.addEventListener('click', (e) => { e.stopPropagation(); cerrarIncidencia(inc.incidenciaId); });
    const btnAtender = li.querySelector('.btn-atender-incidencia');
    if (btnAtender) btnAtender.addEventListener('click', (e) => { e.stopPropagation(); atenderIncidencia(inc.incidenciaId); });
    container.appendChild(li);
  });
}

function renderHistorialTabla(incidencias, tbody) {
  tbody.innerHTML = '';
  if (incidencias.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding:20px;" class="text-muted">No hay incidencias cerradas en el historial.</td></tr>';
    return;
  }
  incidencias.forEach(inc => {
    const tr = document.createElement('tr');
    tr.style.borderBottom = '1px solid #f3f4f6';
    tr.setAttribute('data-id', inc.incidenciaId);
    tr._incData = inc;
    tr.innerHTML = `
      <td>${inc.titulo}</td>
      <td>${inc.nombreCliente || 'Desconocido'}</td>
      <td><span class="badge badge-${(inc.prioridad || 'MEDIA').toLowerCase()}">${inc.prioridad}</span></td>
      <td>${new Date(inc.fechaCreacion).toLocaleDateString()}</td>
      <td><button class="btn-ver-notas-small">Ver Notas</button></td>
    `;
    tr.querySelector('.btn-ver-notas-small').addEventListener('click', (e) => { e.stopPropagation(); abrirNotas(tr._incData, tr); });
    tbody.appendChild(tr);
  });
}

async function abrirNotas(incidencia, elemento) {
  if (!elemento) return;
  if (elemento.querySelector('.notes-container')) { cerrarNotasActuales(); return; }
  cerrarNotasActuales();
  state.incidenciaActiva = incidencia;
  
  const template = q('#notes-template').firstElementChild.cloneNode(true);
  const form = template.querySelector('.chat-form');
  const input = template.querySelector('.chat-input');
  const messagesContainer = template.querySelector('.chat-messages');
  const closeBtn = template.querySelector('.btn-cerrar-notas');
  const statusMsg = template.querySelector('.chat-status-msg');

  closeBtn.addEventListener('click', (e) => { e.stopPropagation(); cerrarNotasActuales(); });
  form.addEventListener('submit', (e) => {
    e.preventDefault();
    const contenido = input.value.trim();
    if (contenido) añadirNota(contenido, messagesContainer, input);
  });

  if (state.usuario.rol === 'CLIENTE') {
    statusMsg.classList.add('hidden');
    form.classList.remove('hidden');
  } else {
    if (incidencia.empleadoId) {
      statusMsg.classList.add('hidden');
      form.classList.remove('hidden');
    } else {
      statusMsg.innerText = 'Debes atender la incidencia para poder añadir notas.';
      statusMsg.classList.remove('hidden');
      form.classList.add('hidden');
    }
  }

  elemento._incData = incidencia;
  elemento.appendChild(template);
  cargarNotas(messagesContainer);
  if (state.notasInterval) clearInterval(state.notasInterval);
  state.notasInterval = setInterval(() => cargarNotas(messagesContainer), 10000);
  elemento.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function cerrarNotasActuales() {
  document.querySelectorAll('.notes-container').forEach(c => { if (!c.closest('#notes-template')) c.remove(); });
  state.incidenciaActiva = null;
  if (state.notasInterval) clearInterval(state.notasInterval);
}

async function cargarNotas(container) {
  if (!state.incidenciaActiva) return;
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
    
    if (notas.length === 0) container.innerHTML = '<div style="text-align:center; padding:20px;" class="text-muted">No hay notas en esta incidencia.</div>';
    
    notas.forEach(nota => {
      const card = document.createElement('div');
      card.className = 'nota-card';
      card.innerHTML = `
        <div class="nota-header">
          <span class="nota-autor">${nota.nombreAutor || nota.autor}</span>
          <span class="nota-fecha">${new Date(nota.fechaCreacion).toLocaleString()}</span>
        </div>
        <div class="nota-contenido">${nota.contenido}</div>
      `;
      container.appendChild(card);
    });

    if (isAtBottom) container.scrollTo({ top: container.scrollHeight, behavior: 'smooth' });
  } catch (err) {
    console.error('Error cargando notas:', err);
  }
}

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
      if ((state.usuario.rol === 'EMPLEADO' || state.usuario.rol === 'ADMINISTRADOR') && !state.incidenciaActiva.empleadoId) {
        atenderIncidencia(state.incidenciaActiva.incidenciaId, true);
      }
    }
  } catch (err) {
    console.error('Error añadiendo nota:', err);
  }
}

async function fetchUsuarios() {
  try {
    const res = await fetch(`${API}/clientes`);
    if (!res.ok) throw new Error('Error al cargar usuarios');
    state.usuarios = await res.json();
    renderUsuarios();
  } catch (err) { console.error(err); }
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
      <td>${u.nombre} ${u.apellido}</td>
      <td>${u.correo}</td>
      <td><span class="badge badge-media">${u.rol}</span></td>
      <td><span style="color: ${u.activo ? '#166534' : '#991b1b'}; font-weight: bold;">${u.activo ? 'ACTIVO' : 'INACTIVO'}</span></td>
      <td>
        <button class="btn-edit-user btn-small" data-id="${u.clienteId}">Editar</button>
        <button class="btn-toggle-user btn-small" data-id="${u.clienteId}" style="background: ${u.activo ? '#991b1b' : '#166534'}">${u.activo ? 'Desactivar' : 'Activar'}</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
  tbody.querySelectorAll('.btn-edit-user').forEach(btn => btn.addEventListener('click', () => abrirFormUsuario(btn.dataset.id)));
  tbody.querySelectorAll('.btn-toggle-user').forEach(btn => btn.addEventListener('click', () => toggleUsuarioActivo(btn.dataset.id)));
}

function abrirFormUsuario(id = null) {
  const form = q('#usuario-form');
  form.reset();
  q('#user-id').value = id || '';
  q('#user-msg').textContent = '';
  show('#usuario-form-view');
  hide('#btn-nuevo-usuario');

  if (id) {
    q('#usuario-form-title').textContent = 'Editar Usuario';
    const u = state.usuarios.find(user => user.clienteId == id);
    if (u) {
      q('#user-nombre').value = u.nombre;
      q('#user-apellido').value = u.apellido;
      q('#user-email').value = u.correo;
      q('#user-rol').value = u.rol;
      q('#user-activo').value = String(u.activo);
      q('#label-user-pass').textContent = 'Cambiar Contraseña';
      show('#pass-help');
      q('#user-pass').required = false;
    }
  } else {
    q('#usuario-form-title').textContent = 'Nuevo Usuario';
    q('#label-user-pass').textContent = 'Contraseña';
    hide('#pass-help');
    q('#user-pass').required = true;
  }
}

async function toggleUsuarioActivo(id) {
  const u = state.usuarios.find(user => user.clienteId == id);
  if (!u) return;
  const confirmMsg = u.activo ? `¿Estás seguro de que deseas desactivar a ${u.nombre}? No podrá iniciar sesión.` : `¿Deseas activar a ${u.nombre}?`;
  if (!confirm(confirmMsg)) return;
  try {
    const res = await fetch(`${API}/clientes/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ...u, activo: !u.activo })
    });
    if (res.ok) { showNotification('Estado del usuario actualizado'); fetchUsuarios(); }
    else showNotification('Error al cambiar estado del usuario');
  } catch (err) { console.error(err); }
}

async function cargarUsuariosAdmin() {
  const tbody = q('#usuarios-body');
  tbody.innerHTML = '<tr><td colspan="4">Cargando...</td></tr>';
  try {
    const res = await fetch(`${API}/clientes`);
    if (!res.ok) throw new Error('No se pudieron cargar los usuarios');
    const usuarios = await res.json();
    tbody.innerHTML = '';
    usuarios.forEach(user => {
      const tr = document.createElement('tr');
      tr.style.borderBottom = '1px solid #f3f4f6';
      const actions = user.rol === 'CLIENTE' ? `<button onclick="cambiarRol(${user.clienteId}, 'EMPLEADO')">Hacer Empleado</button>` : user.rol === 'EMPLEADO' ? `<button onclick="cambiarRol(${user.clienteId}, 'CLIENTE')">Hacer Cliente</button>` : '<em>Admin</em>';
      tr.innerHTML = `<td>${user.nombre} ${user.apellido}</td><td>${user.correo}</td><td><span class="badge badge-media">${user.rol}</span></td><td>${actions}</td>`;
      tbody.appendChild(tr);
    });
  } catch (error) { tbody.innerHTML = `<tr><td colspan="4">Error: ${error.message}</td></tr>`; }
}

async function cambiarRol(id, nuevoRol) {
  try {
    const userRes = await fetch(`${API}/clientes/${id}`);
    const user = await userRes.json();
    user.rol = nuevoRol;
    const res = await fetch(`${API}/clientes/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(user)
    });
    if (res.ok) cargarUsuariosAdmin();
    else alert('Error al cambiar el rol');
  } catch (err) { console.error(err); alert('Error de conexión'); }
}

function onLogged(usuario) {
  state.usuario = usuario;
  localStorage.setItem('tfg_usuario', JSON.stringify(usuario));
  q('#welcome').textContent = `Bienvenido/a, ${usuario.nombre} ${usuario.apellido}`;
  q('#role-info').textContent = `Rol: ${usuario.rol}`;
  hide('#auth-view');
  hide('#register-view');
  show('#app-view');
  document.querySelectorAll('.role-view').forEach(v => v.classList.add('hidden'));

  if (usuario.rol === 'ADMINISTRADOR') {
    show('#admin-view');
    fetchUsuarios();
    fetchIncidencias(`${API}/incidencias`, '#admin-incidencias-list', '#admin-historial-incidencias-body');
  } else if (usuario.rol === 'EMPLEADO') {
    show('#empleado-view');
    fetchIncidencias(`${API}/incidencias`, '#todas-incidencias-list');
  } else {
    show('#cliente-view');
    fetchIncidencias(`${API}/incidencias/cliente/${usuario.clienteId}`, '#incidencias-list');
  }
}

window.cambiarRol = cambiarRol;
window.atenderIncidencia = atenderIncidencia;
window.cerrarIncidencia = cerrarIncidencia;
window.abrirNotas = abrirNotas;
window.cerrarNotasActuales = cerrarNotasActuales;
window.fetchIncidencias = fetchIncidencias;

q('#btn-to-register').addEventListener('click', () => { hide('#auth-view'); show('#register-view'); q('#login-msg').textContent = ''; });

q('#login-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  if (!validateForm(e.target)) return;
  const email = q('#login-email').value.trim();
  const pass = q('#login-pass').value.trim();
  q('#login-msg').textContent = '';
  try {
    const usuario = await login(email, pass);
    if (usuario) onLogged(usuario);
    else q('#login-msg').textContent = 'Credenciales incorrectas o la cuenta no existe.';
  } catch (err) { q('#login-msg').textContent = err.message; }
});

q('#register-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  if (!validateForm(e.target)) return;
  const data = {
    nombre: q('#reg-nombre').value.trim(),
    apellido: q('#reg-apellido').value.trim(),
    correo: q('#reg-email').value.trim(),
    contrasena: q('#reg-pass').value.trim(),
    activo: true,
    rol: 'CLIENTE'
  };
  q('#reg-msg').textContent = '';
  try { onLogged(await registrarCliente(data)); }
  catch (err) { q('#reg-msg').textContent = err.message; }
});

q('#reg-cancel').addEventListener('click', () => { hide('#register-view'); show('#auth-view'); });

q('#logout').addEventListener('click', () => {
  state.usuario = null;
  localStorage.removeItem('tfg_usuario');
  cerrarNotasActuales();
  hide('#app-view');
  show('#auth-view');
  q('#login-form').reset();
  q('#register-form').reset();
  document.querySelectorAll('.create-incidencia-view').forEach(v => v.classList.add('hidden'));
  document.querySelectorAll('.btn-nueva-incidencia').forEach(b => b.style.display = 'block');
});

document.querySelectorAll('.tab-btn').forEach(btn => {
  btn.addEventListener('click', () => {
    cerrarNotasActuales();
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(c => c.classList.add('hidden'));
    btn.classList.add('active');
    show(`#${btn.dataset.tab}`);
  });
});

document.querySelectorAll('.btn-nueva-incidencia').forEach(btn => {
  btn.addEventListener('click', (e) => {
    const view = e.target.closest('.role-view');
    view.querySelector('.create-incidencia-view').classList.remove('hidden');
    e.target.style.display = 'none';
    view.querySelector('.incidencia-form').reset();
    view.querySelector('.inc-msg').textContent = '';
  });
});

document.querySelectorAll('.inc-cancel').forEach(btn => {
  btn.addEventListener('click', (e) => {
    const view = e.target.closest('.role-view');
    view.querySelector('.create-incidencia-view').classList.add('hidden');
    view.querySelector('.btn-nueva-incidencia').style.display = 'block';
  });
});

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
    try {
      await crearIncidencia(data);
      view.querySelector('.create-incidencia-view').classList.add('hidden');
      view.querySelector('.btn-nueva-incidencia').style.display = 'block';
      if (state.usuario.rol === 'CLIENTE') fetchIncidencias(`${API}/incidencias/cliente/${state.usuario.clienteId}`, '#incidencias-list', '#historial-incidencias-list');
    } catch (err) { view.querySelector('.inc-msg').textContent = err.message; }
  });
});

q('#search-usuarios').addEventListener('input', (e) => { renderUsuarios(e.target.value); });
q('#user-email').addEventListener('input', (e) => {
  const email = e.target.value.trim().toLowerCase();
  q('#user-rol').value = email.endsWith('@incidencias.com') ? 'EMPLEADO' : 'CLIENTE';
});

q('#btn-nuevo-usuario').addEventListener('click', () => abrirFormUsuario());
q('#user-cancel').addEventListener('click', () => { hide('#usuario-form-view'); show('#btn-nuevo-usuario'); });

q('#usuario-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  if (!validateForm(e.target)) return;
  const id = q('#user-id').value;
  const msgDiv = q('#user-msg');
  msgDiv.textContent = '';
  const data = {
    nombre: q('#user-nombre').value.trim(),
    apellido: q('#user-apellido').value.trim(),
    correo: q('#user-email').value.trim(),
    activo: q('#user-activo').value === 'true'
  };
  const pass = q('#user-pass').value.trim();
  if (pass) data.contrasena = pass;
  
  try {
    const url = id ? `${API}/clientes/${id}` : `${API}/clientes`;
    const method = id ? 'PUT' : 'POST';
    const res = await fetch(url, { method: method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(data) });
    if (res.ok) { hide('#usuario-form-view'); show('#btn-nuevo-usuario'); fetchUsuarios(); }
    else msgDiv.textContent = 'Error: ' + await res.text();
  } catch (err) { msgDiv.textContent = 'Error de conexión'; }
});

document.addEventListener('DOMContentLoaded', () => {
  const savedUser = localStorage.getItem('tfg_usuario');
  if (savedUser) {
    try { onLogged(JSON.parse(savedUser)); }
    catch (e) { localStorage.removeItem('tfg_usuario'); }
  }
  const eyeOpenSVG = `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>`;
  const eyeClosedSVG = `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>`;
  document.querySelectorAll('.toggle-password').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const btnElement = e.currentTarget;
      const input = btnElement.closest('.password-wrapper').querySelector('input');
      if (input.type === 'password') { input.type = 'text'; btnElement.innerHTML = eyeClosedSVG; }
      else { input.type = 'password'; btnElement.innerHTML = eyeOpenSVG; }
    });
  });
});
