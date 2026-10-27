const { validateAdminToken } = require('@/lib/auth');

export async function POST(request) {
  try {
    const body = await request.json();
    const userInput = (body.username || '').trim();
    const password = (body.password || '').trim();

    const isAdminUser = userInput === 'admin' || userInput === 'admin@home.dev';
    const isValidPassword = password === 'admin123';

    if (!isAdminUser || !isValidPassword) {
      return Response.json({ message: 'Invalid admin credentials' }, { status: 401 });
    }

    return Response.json({
      message: 'Login successful',
      user: {
        username: 'admin',
        email: 'admin@home.dev',
        fullName: 'System Admin',
      },
      token: 'ems-admin-local-token',
    });
  } catch (error) {
    return Response.json({ message: 'Login failed' }, { status: 500 });
  }
}
