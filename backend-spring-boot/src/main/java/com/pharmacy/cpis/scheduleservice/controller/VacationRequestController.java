package com.pharmacy.cpis.scheduleservice.controller;

import java.util.Collection;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pharmacy.cpis.scheduleservice.dto.AddVacationRequestDTO;
import com.pharmacy.cpis.scheduleservice.dto.VacationRequestDTO;
import com.pharmacy.cpis.scheduleservice.dto.VacationReviewDTO;
import com.pharmacy.cpis.scheduleservice.model.workschedule.VacationRequest;
import com.pharmacy.cpis.scheduleservice.service.IVacationRequestService;
import com.pharmacy.cpis.userservice.model.users.UserAccount;
import com.pharmacy.cpis.util.CollectionUtil;
import com.pharmacy.cpis.util.aspects.EmployeeAccountActive;

@RestController
@RequestMapping(value = "api/vacationrequst")
public class VacationRequestController {
	@Autowired
	private IVacationRequestService vacationRequestService;

	@PostMapping("/createrequest")
	@PreAuthorize("hasRole('PHARMACIST')")
	public ResponseEntity<AddVacationRequestDTO> getAllConsultationsForConsultant(
			@RequestBody AddVacationRequestDTO vacationRequestDTO) {

		AddVacationRequestDTO vacationRequest = vacationRequestService.createVacationRequest(vacationRequestDTO);

		return new ResponseEntity<>(vacationRequest, HttpStatus.OK);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('PHARMACY_ADMIN', 'ADMIN')")
	@EmployeeAccountActive
	public ResponseEntity<Collection<VacationRequestDTO>> getVacationRequests(Authentication authentication) {
		UserAccount user = (UserAccount) authentication.getPrincipal();

		Collection<VacationRequest> requests = vacationRequestService.getVacationRequests(user);

		Collection<VacationRequestDTO> mapped = CollectionUtil.map(requests,
				request -> new VacationRequestDTO(request));

		return ResponseEntity.ok(mapped);
	}

	@PostMapping(value = "/{id}/reject", consumes = "application/json")
	@PreAuthorize("hasAnyRole('PHARMACY_ADMIN', 'ADMIN')")
	@EmployeeAccountActive
	public ResponseEntity<Void> rejectVacationRequest(@PathVariable(required = true) Long id,
			@RequestBody @Valid VacationReviewDTO response, Authentication authentication) {
		UserAccount user = (UserAccount) authentication.getPrincipal();

		vacationRequestService.reject(user, id, response.getResponse());

		return ResponseEntity.noContent().build();
	}

	@PostMapping(value = "/{id}/accept", consumes = "application/json")
	@PreAuthorize("hasAnyRole('PHARMACY_ADMIN', 'ADMIN')")
	@EmployeeAccountActive
	public ResponseEntity<Void> acceptVacationRequest(@PathVariable(required = true) Long id,
			@RequestBody @Valid VacationReviewDTO response, Authentication authentication) {
		UserAccount user = (UserAccount) authentication.getPrincipal();

		vacationRequestService.accept(user, id, response.getResponse());

		return ResponseEntity.noContent().build();
	}

}
