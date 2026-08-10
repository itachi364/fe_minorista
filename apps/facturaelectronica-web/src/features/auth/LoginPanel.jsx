import { Field } from '../../components/forms.jsx';

export function LoginPanel({ form, setForm, busy, onLogin }) {
  return (
    <section className="top-panel login-panel">
      <form className="login-form" onSubmit={(event) => { event.preventDefault(); onLogin(); }}>
        <Field label="Correo electronico" value={form.email} onChange={(value) => setForm({ ...form, email: value })} type="email" placeholder="usuario@empresa.com" autoComplete="username" />
        <Field label="Contrasena" value={form.password} onChange={(value) => setForm({ ...form, password: value })} type="password" placeholder="Ingresa tu contrasena" autoComplete="current-password" />
        <button className="primary" disabled={busy} type="submit">Ingresar</button>
      </form>
    </section>
  );
}
