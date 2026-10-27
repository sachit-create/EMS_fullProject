const { getEmployees, buildEmployeeFromRow, updateEmployee, deleteEmployee } = require('@/lib/db');
const { validateAdminToken, normalizeEmployeePayload } = require('@/lib/auth');

function parseAuthHeader(request) {
  return request.headers.get('authorization') || '';
}

export async function GET(request, { params }) {
  if (!validateAdminToken(parseAuthHeader(request))) {
    return Response.json({ message: 'Unauthorized' }, { status: 401 });
  }

  const employees = getEmployees();
  const employee = employees.find((item) => Number(item.id) === Number(params.id));

  if (!employee) {
    return Response.json({ message: 'Employee not found' }, { status: 404 });
  }

  return Response.json({ employee: buildEmployeeFromRow(employee) });
}

export async function PUT(request, { params }) {
  if (!validateAdminToken(parseAuthHeader(request))) {
    return Response.json({ message: 'Unauthorized' }, { status: 401 });
  }

  try {
    const payload = normalizeEmployeePayload(await request.json());
    const employee = updateEmployee(Number(params.id), payload);

    if (!employee) {
      return Response.json({ message: 'Employee not found' }, { status: 404 });
    }

    return Response.json({ message: 'Employee updated successfully', employee: buildEmployeeFromRow(employee) });
  } catch (error) {
    return Response.json({ message: error.message || 'Could not update employee' }, { status: 500 });
  }
}

export async function DELETE(request, { params }) {
  if (!validateAdminToken(parseAuthHeader(request))) {
    return Response.json({ message: 'Unauthorized' }, { status: 401 });
  }

  const employees = getEmployees();
  const found = employees.find((item) => Number(item.id) === Number(params.id));

  if (!found) {
    return Response.json({ message: 'Employee not found' }, { status: 404 });
  }

  deleteEmployee(Number(params.id));
  return Response.json({ message: 'Employee deleted successfully' });
}
