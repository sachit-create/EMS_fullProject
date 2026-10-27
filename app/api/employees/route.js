const { getEmployees, buildEmployeeFromRow, updateEmployee, deleteEmployee, addEmployee } = require('@/lib/db');
const { validateAdminToken, normalizeEmployeePayload } = require('@/lib/auth');

function parseAuthHeader(request) {
  return request.headers.get('authorization') || '';
}

export async function GET(request) {
  if (!validateAdminToken(parseAuthHeader(request))) {
    return Response.json({ message: 'Unauthorized' }, { status: 401 });
  }

  const employees = getEmployees();
  return Response.json({ employees: employees.map(buildEmployeeFromRow) });
}

export async function POST(request) {
  if (!validateAdminToken(parseAuthHeader(request))) {
    return Response.json({ message: 'Unauthorized' }, { status: 401 });
  }

  try {
    const payload = await request.json();
    const normalized = normalizeEmployeePayload(payload);

    if (!normalized.name || !normalized.father_name || !normalized.email) {
      return Response.json({ message: 'Name, father name and email are required.' }, { status: 400 });
    }

    const employee = addEmployee({
      ...normalized,
      employee_code: normalized.employee_code || `EMP-${String((getEmployees().length || 0) + 1).padStart(4, '0')}`,
    });

    return Response.json({ message: 'Employee created successfully', employee: buildEmployeeFromRow(employee) }, { status: 201 });
  } catch (error) {
    return Response.json({ message: error.message || 'Could not create employee' }, { status: 500 });
  }
}
