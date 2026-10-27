const { db } = require('@/lib/db');
const { validateAdminToken } = require('@/lib/auth');
const { generateEmployeeSummary } = require('@/lib/ai');

export async function POST(request) {
  if (!validateAdminToken(request.headers.get('authorization') || '')) {
    return Response.json({ message: 'Unauthorized' }, { status: 401 });
  }

  try {
    const body = await request.json();
    const employee = body.employee || {};

    if (!employee.name || !employee.email) {
      return Response.json({ message: 'Employee details are required for summary generation' }, { status: 400 });
    }

    const summary = await generateEmployeeSummary(employee);
    return Response.json({ summary });
  } catch (error) {
    return Response.json({ message: error.message || 'Summary generation failed' }, { status: 500 });
  }
}
