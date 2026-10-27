const { db } = require('@/lib/db');
const { ADMIN_TOKEN, normalizeEmployeePayload, buildEmployeeFromRow, buildEmployeeCode } = require('@/lib/auth');

async function generateEmployeeSummary(employee) {
  const baseUrl = process.env.OLLAMA_BASE_URL || 'http://localhost:11434';
  const model = process.env.OLLAMA_MODEL || 'llama3.2:3b';

  const payload = {
    model,
    prompt: `You are an HR assistant. Write exactly 4 short bullet points summarizing this employee profile. Use only the provided facts. Do not invent information. Format the answer as plain text with bullet markers like "-" and keep it concise.\n\nName: ${employee.name || 'N/A'}\nEmployee Code: ${employee.employeeCode || 'N/A'}\nFather Name: ${employee.fatherName || 'N/A'}\nEmail: ${employee.email || 'N/A'}\nGender: ${employee.gender || 'N/A'}\nSalary: ₹${Number(employee.salary || 0).toLocaleString('en-IN')}\nDepartment: ${employee.department || 'N/A'}\nDesignation: ${employee.designation || 'N/A'}`,
    stream: false,
    options: {
      temperature: 0.3,
      num_predict: 160,
    },
  };

  try {
    const res = await fetch(`${baseUrl}/api/generate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      throw new Error('Ollama request failed');
    }

    const data = await res.json();
    const text = (data?.response || '').trim();

    if (text) {
      return text
        .split('\n')
        .map((line) => line.trim())
        .filter(Boolean)
        .slice(0, 4)
        .join('\n');
    }
  } catch (error) {
    console.warn('Ollama unavailable, using fallback summary', error.message);
  }

  return [
    `PROFILE: ${employee.name || 'Employee'} is an employee with code ${employee.employeeCode || 'N/A'} and belongs to ${employee.department || 'General'} department.`,
    `PAYROLL: This profile reflects a current salary of ₹${Number(employee.salary || 0).toLocaleString('en-IN')} with the role of ${employee.designation || 'Employee'}.`,
    `DATA: The employee's father name is ${employee.fatherName || 'not provided'}, and the contact email is ${employee.email || 'not provided'}.`,
    `ACTION: This summary is generated from official employee data and can be used for HR review, payroll checks, and profile tracking.`,
  ].join('\n');
}

module.exports = { generateEmployeeSummary };
