package com.locadora_rdt_backend.modules.organization.employees.dto;

import com.locadora_rdt_backend.modules.organization.employees.constants.EmployeeConstants;
import com.locadora_rdt_backend.modules.organization.employees.validation.EmployeeInsertValid;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@EmployeeInsertValid
public class EmployeeInsertDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Size(min = EmployeeConstants.NAME_MIN_LENGTH, max = EmployeeConstants.NAME_MAX_LENGTH,
            message = EmployeeConstants.NAME_LENGTH)
    @NotBlank(message = EmployeeConstants.NAME_REQUIRED)
    private String name;

    @NotBlank(message = EmployeeConstants.EMPLOYEE_CODE_REQUIRED)
    @Size(min = EmployeeConstants.EMPLOYEE_CODE_MIN_LENGTH, max = EmployeeConstants.EMPLOYEE_CODE_MAX_LENGTH,
            message = EmployeeConstants.EMPLOYEE_CODE_LENGTH)
    private String employeeCode;

    @Email(message = EmployeeConstants.EMAIL_INVALID)
    @Size(max = EmployeeConstants.EMAIL_MAX_LENGTH, message = EmployeeConstants.EMAIL_MAX_LENGTH_MESSAGE)
    private String email;

    @Size(max = EmployeeConstants.PHONE_MAX_LENGTH, message = EmployeeConstants.PHONE_MAX_LENGTH_MESSAGE)
    private String phone;

    @Size(max = EmployeeConstants.ADDRESS_MAX_LENGTH, message = EmployeeConstants.ADDRESS_MAX_LENGTH_MESSAGE)
    private String address;

    @DecimalMin(value = EmployeeConstants.MINIMUM_SALARY, inclusive = false,
            message = EmployeeConstants.SALARY_MUST_BE_POSITIVE)
    @Digits(integer = EmployeeConstants.SALARY_MAX_INTEGER_DIGITS,
            fraction = EmployeeConstants.SALARY_MAX_FRACTION_DIGITS, message = EmployeeConstants.SALARY_INVALID)
    private BigDecimal salary;

    @NotNull(message = EmployeeConstants.HIRE_DATE_REQUIRED)
    private LocalDate hireDate;

    private LocalDate terminationDate;

    @NotBlank(message = EmployeeConstants.CONTRACT_TYPE_REQUIRED)
    @Size(max = EmployeeConstants.CONTRACT_TYPE_MAX_LENGTH,
            message = EmployeeConstants.CONTRACT_TYPE_MAX_LENGTH_MESSAGE)
    private String employmentType;

    private Boolean active;

    @NotNull(message = EmployeeConstants.POSITION_REQUIRED)
    private Long positionId;

    @NotNull(message = EmployeeConstants.DEPARTMENT_REQUIRED)
    private Long departmentId;

    public EmployeeInsertDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(LocalDate terminationDate) {
        this.terminationDate = terminationDate;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}
