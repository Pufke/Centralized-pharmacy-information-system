package com.pharmacy.cpis.scheduleservice.model.workschedule;

import com.pharmacy.cpis.userservice.model.users.Consultant;
import com.pharmacy.cpis.util.DateRange;

import javax.persistence.*;

@Entity
public class VacationRequest {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Embedded
	private DateRange dateRange;

	@Column(nullable = false)
	private VacationRequestStatus status;

	@Column(length = 500)
	private String response;

	@ManyToOne(optional = false)
	private Consultant consultant;

	public VacationRequest() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DateRange getDateRange() {
		return dateRange;
	}

	public void setDateRange(DateRange dateRange) {
		this.dateRange = dateRange;
	}

	public VacationRequestStatus getStatus() {
		return status;
	}

	public void setStatus(VacationRequestStatus status) {
		this.status = status;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public Consultant getConsultant() {
		return consultant;
	}

	public void setConsultant(Consultant consultant) {
		this.consultant = consultant;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VacationRequest other = (VacationRequest) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
