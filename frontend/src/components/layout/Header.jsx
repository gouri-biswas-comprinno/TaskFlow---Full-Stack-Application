export default function Header() {
  return (
    <header className="site-header">
      <a aria-label="TaskFlow home" className="brand" href="/">
        <span aria-hidden="true" className="brand-mark">T</span>
        <span>TaskFlow</span>
      </a>
      <span className="header-note">A little more focus, every day.</span>
    </header>
  );
}
