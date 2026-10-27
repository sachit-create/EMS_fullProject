import './globals.css';

export const metadata = {
  title: 'EMS Dashboard',
  description: 'Employee management system built with Next.js and local SQLite',
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
