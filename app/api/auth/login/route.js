const { validateAdminToken } = require('@/lib/auth');

export async function POST(request) {
  try {
    const body = await request.json();
    const username = (body.username || '').trim();
    const password = (body.password || '').trim();

    const isAdmin = (username === 'admin' || username === 'admin@home.dev') && password === 'admin123';

    if (!isAdmin) {
      return Response.json({ message: 'Invalid admin credentials' }, { status: 401 });
    }

    return Response.json({
      message: 'Login successful',
      token: 'ems-admin-local-token',
      user: {
        username: 'admin',
        email: 'admin@home.dev',
        fullName: 'System Admin',
      },
    });
  } catch (error) {
    return Response.json({ message: error.message || 'Login failed' }, { status: 500 });
  }
}
