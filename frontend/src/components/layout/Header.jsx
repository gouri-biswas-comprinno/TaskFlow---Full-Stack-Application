export default function Header({ userEmail }) {
  if (userEmail) {
    return (
      <header className="site-header top-navigation">
        <div>
          <p className="eyebrow">PERSONAL WORKSPACE</p>
          <h1 className="top-navigation-title">Task workspace</h1>
        </div>
        <span className="top-navigation-account">{userEmail}</span>
      </header>
    );
  }

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
