/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

package org.gluu.oxtrust.model.scim2;

/**
 * Java class for extension enterprise.
 */
public final class Enterprise { // NOSONAR - this class is instantiable with the builder

    private final String employeeNumber;
    private final String costCenter;
    private final String organization;
    private final String division;
    private final String department;
    private final Manager manager;

    private Enterprise(Builder builder) {
        this.employeeNumber = builder.employeeNumber;
        this.costCenter = builder.costCenter;
        this.organization = builder.organization;
        this.division = builder.division;
        this.department = builder.department;
        this.manager = builder.manager;
    }

    public static class Builder {
        private String employeeNumber;
        private String costCenter;
        private String organization;
        private String division;
        private String department;
        private Manager manager;

        public Builder setEmployeeNumber(String employeeNumber) {
            this.employeeNumber = employeeNumber;
            return this;
        }

        public Builder setCostCenter(String costCenter) {
            this.costCenter = costCenter;
            return this;

        }

        public Builder setOrganization(String organization) {
            this.organization = organization;
            return this;

        }

        public Builder setDivision(String division) {
            this.division = division;
            return this;

        }

        public Builder setDepartment(String department) {
            this.department = department;
            return this;

        }

        public Builder setManager(Manager manager) {
            this.manager = manager;
            return this;
        }

        public Enterprise build() {
            return new Enterprise(this);
        }
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getCostCenter() {
        return costCenter;
    }

    public String getOrganization() {
        return organization;
    }

    public String getDivision() {
        return division;
    }

    public String getDepartment() {
        return department;
    }

    public Manager getManager() {
        return manager;
    }

}
