package com.pharmacy.cpis.userservice.service;

import com.pharmacy.cpis.userservice.model.users.Patient;

import java.util.List;

public interface IPatientService {
    List<Patient> findAllPatient();
}
