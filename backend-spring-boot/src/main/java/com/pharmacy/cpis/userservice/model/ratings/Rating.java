package com.pharmacy.cpis.userservice.model.ratings;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Rating {
	@Column(nullable = false)
	private Integer rating;

	public Rating() {
		super();
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}
}
