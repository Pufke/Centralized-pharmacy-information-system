package com.pharmacy.cpis.drugservice.service.impl;

import java.util.*;

import com.pharmacy.cpis.pharmacyservice.repository.IPharmacyRepository;
import com.pharmacy.cpis.pharmacyservice.service.IPharmacyService;
import com.pharmacy.cpis.userservice.model.users.Patient;
import com.pharmacy.cpis.userservice.repository.IPatientRepository;
import com.pharmacy.cpis.userservice.service.IPatientService;
import com.pharmacy.cpis.userservice.service.impl.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pharmacy.cpis.drugservice.dto.DrugDTO;
import com.pharmacy.cpis.drugservice.dto.DrugRegisterDTO;
import com.pharmacy.cpis.drugservice.dto.DrugSpecificationDTO;
import com.pharmacy.cpis.drugservice.model.drug.Drug;
import com.pharmacy.cpis.drugservice.model.drug.DrugClass;
import com.pharmacy.cpis.drugservice.model.drug.DrugForm;
import com.pharmacy.cpis.drugservice.model.drug.DrugSpecification;
import com.pharmacy.cpis.drugservice.model.drug.Ingredient;
import com.pharmacy.cpis.drugservice.model.drugsales.AvailableDrug;
import com.pharmacy.cpis.drugservice.repository.IAvailableDrugRepository;
import com.pharmacy.cpis.drugservice.repository.IDrugClassRepository;
import com.pharmacy.cpis.drugservice.repository.IDrugFormRepository;
import com.pharmacy.cpis.drugservice.repository.IDrugRatingRepository;
import com.pharmacy.cpis.drugservice.repository.IDrugRepository;
import com.pharmacy.cpis.drugservice.repository.IDrugSpecificationRepository;
import com.pharmacy.cpis.drugservice.repository.IIngredientsRepository;
import com.pharmacy.cpis.drugservice.service.IDrugService;
import com.pharmacy.cpis.userservice.model.ratings.DrugRating;
import com.pharmacy.cpis.util.exceptions.PSConflictException;

@Service
public class DrugService implements IDrugService {

	@Autowired
	private IDrugClassRepository drugClassRepository;

	@Autowired
	private IDrugFormRepository drugFormRepository;

	@Autowired
	private IDrugSpecificationRepository drugSpecificationRepository;

	@Autowired
	private IDrugRepository drugRepository;

	@Autowired
	private IIngredientsRepository ingredientsRepository;

	@Autowired
	private IDrugRatingRepository drugRatingRepository;

	@Autowired
	private IAvailableDrugRepository availableDrugRepository;

	@Autowired
	private IPatientRepository patientRepository;


	@Override
	public Drug registerDrug(DrugRegisterDTO drug) {
		if (drugRepository.existsByCode(drug.getDrug().getCode()))
			throw new PSConflictException("A drug with this code already exists");
		Drug addedDrug = addNewDrug(drug.getDrug());
		DrugSpecification addedSpecification = addNewDrugSpecification(drug.getSpecification(), addedDrug);

		return addedDrug;
	}

	@Override
	public List<Drug> findAll() {
		return drugRepository.findAll();
	}

	@Override
	public List<DrugClass> findAllDrugClass() {
		return drugClassRepository.findAll();
	}

	@Override
	public Double getMarkOfDrug(Drug drug) {
		double mark = 0.0;
		List<DrugRating> ratings = drugRatingRepository.findByDrugCode(drug.getCode());
		for (DrugRating rating : ratings) {
			mark += rating.getRating().getRating();
		}
		return mark == 0.0 ? 0 : mark / ratings.size();
	}

	@Override
	public DrugSpecification getDrugSpecificationByDrugCode(String drugCode) {
		return drugSpecificationRepository.findByDrugCode(drugCode);
	}

	@Override
	public List<AvailableDrug> findAvailableDrugsByCode(String drugCode) {
		return availableDrugRepository.findByDrugCode(drugCode);
	}

	private DrugSpecification addNewDrugSpecification(DrugSpecificationDTO specification, Drug addedDrug) {
		DrugSpecification newDrugSpecification = new DrugSpecification();
		newDrugSpecification.setManufacturer(specification.getManufacturer());
		newDrugSpecification.setContraindications(specification.getContraindications());
		newDrugSpecification.setRecommendedDailyDose(specification.getRecommendedDailyDose());
		newDrugSpecification.setDrug(addedDrug);
		newDrugSpecification.setPrescriptionRequired(false);
		DrugSpecification addedDrugSpecification = drugSpecificationRepository.save(newDrugSpecification);

		// This is after first save because i want to get id of added drug specification
		// to can add ingredient(he need specification id)
		if (specification.getIngredients() != null && !specification.getIngredients().isEmpty()) {
			addedDrugSpecification
					.setIngredients(getAndSaveSpecificationIngredients(specification, addedDrugSpecification));
			addedDrugSpecification = drugSpecificationRepository.save(addedDrugSpecification);
		}

		return addedDrugSpecification;
	}

	private Set<Ingredient> getAndSaveSpecificationIngredients(DrugSpecificationDTO specification,
			DrugSpecification addedDrugSpecification) {
		Set<Ingredient> ingredients = new HashSet<Ingredient>();
		for (Ingredient ingredient : specification.getIngredients()) {
			Ingredient newIngredient = new Ingredient();
			newIngredient.setSpecification(addedDrugSpecification);
			newIngredient.setName(ingredient.getName());
			newIngredient.setAmount(ingredient.getAmount());
			ingredients.add(ingredientsRepository.save(newIngredient));
		}
		return ingredients;
	}

	private Drug addNewDrug(DrugDTO drug) {
		Drug newDrug = new Drug();
		newDrug.setName(drug.getName());
		newDrug.setCode(drug.getCode());
		newDrug.setLoyaltyPoints(Integer.parseInt(drug.getLoyaltyPoints()));
		DrugClass drugClass = drugClassRepository.findByName(drug.getTypeOfDrug());
		newDrug.setDrugClass(drugClass);
		// TODO: Choose which form to put, if there is no one from frontend
		// for now is this
		DrugForm drugForm = drugFormRepository.findByName("Tablet");
		newDrug.setDrugForm(drugForm);

		if (drug.getAlternateDrugs() != null && !drug.getAlternateDrugs().isEmpty())
			newDrug.setAlternateDrugs(getAlternateDrugs(drug));

		Drug addedDrug = drugRepository.save(newDrug);
		return addedDrug;
	}

	private Set<Drug> getAlternateDrugs(DrugDTO drug) {
		Set<Drug> drugAlternateDrugs = new HashSet<Drug>();
		for (DrugDTO alternateDrug : drug.getAlternateDrugs()) {
			Drug realDrug = drugRepository.findByCode(alternateDrug.getCode());
			if (realDrug != null)
				drugAlternateDrugs.add(realDrug);
		}
		return drugAlternateDrugs;
	}

	public Collection<DrugForm> findAllDrugForms() {
		return drugFormRepository.findAll();
	}

	@Override
	public List<DrugDTO> getDrugsForPhatientWithoutAlergies(Long patientID, IDrugService drugService) {

		List<Drug> allDrugs = drugRepository.findAll();
		List<Drug> allDrugsWithoutAlergies = new ArrayList<>(allDrugs);

		Patient patient = patientRepository.getOne(patientID);
		Set<Drug> allAllergies = patient.getAllergies();

		for(Drug drug: allDrugs){
			for(Drug allergies: allAllergies){
				if(drug.getCode().equals(allergies.getCode())){
					allDrugsWithoutAlergies.remove(drug);
				}
			}
		}

		List<DrugDTO> allDrugsWithoutAlergiesDTOs = new ArrayList<>();
		//Convert to dto
		for(Drug d: allDrugsWithoutAlergies){
			allDrugsWithoutAlergiesDTOs.add(new DrugDTO(d, drugService));
		}

		return allDrugsWithoutAlergiesDTOs;
	}
}
