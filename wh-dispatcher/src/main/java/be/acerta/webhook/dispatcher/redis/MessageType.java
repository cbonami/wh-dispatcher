package be.acerta.webhook.dispatcher.redis;

import static java.util.Arrays.stream;

import java.util.Optional;

public enum MessageType {
    // AANVRAAGPERIODE_AANGEMAAKT("AanvraagPeriodeAangemaakt"),
    // AANVRAAGPERIODE_GEWIJZIGD("AanvraagPeriodeGewijzigd"),
    // AANVRAAGPERIODE_VERWIJDERD("AanvraagPeriodeVerwijderd"),
    // LOCATIE_GEWIJZIGD("LocatieGewijzigd"),
    // LOCATIE_TOEWIJZING_AANGEMAAKT("LocatieToewijzingAangemaakt"),
    // LOCATIE_TOEWIJZING_GEWIJZIGD("LocatieToewijzingGewijzigd"),
    // LOCATIE_TOEWIJZING_VERWIJDERD("LocatieToewijzingVerwijderd"),
    // MEDEWERKER_IAM_GEWIJZIGD("MedewerkerIamGewijzigd"),
    // MEDEWERKER_IAM_TOEWIJZING_AANGEMAAKT("MedewerkerIamToewijzingAangemaakt"),
    // MEDEWERKER_IAM_TOEWIJZING_GEWIJZIGD("MedewerkerIamToewijzingGewijzigd"),
    // MEDEWERKER_IAM_TOEWIJZING_VERWIJDERD("MedewerkerIamToewijzingVerwijderd"),
    // ORGANISATIE_TOEWIJZING_AANGEMAAKT("OrganisatieToewijzingAangemaakt"),
    // ORGANISATIE_TOEWIJZING_VERWIJDERD("OrganisatieToewijzingVerwijderd"),
    // PROJECTONDERDEEL_AANGEMAAKT("ProjectonderdeelAangemaakt"),
    // PROJECTONDERDEEL_GEWIJZIGD("ProjectonderdeelGewijzigd"),
    // PROJECTONDERDEEL_VERWIJDERD("ProjectonderdeelVerwijderd"),
    // DIENSTDEFINITIE_TOEGEVOEGD("DienstDefinitieToegevoegd"),
    // DIENSTDEFINITIE_GEWIJZIGD("DienstDefinitieGewijzigd"),
    // EMPLOYEE_ACTIVATIONS_REQUESTED("EmployeeActivationsRequested", false),
    // OFFICE_CREATED("OfficeCreated"),
    // OFFICE_UPDATED("OfficeUpdated"),
    // OFFICE_DELETED("OfficeDeleted"),
    // EMPLOYEE_CREATED("EmployeeCreated"),
    // EMPLOYEE_DELETED("EmployeeDeleted"),
    // USER_CREATED("UserCreated"),
    // USER_UPDATED("UserUpdated"),
    // USER_DELETED("UserDeleted"),
    // OFFICE_MANAGER_CREATED("OfficeManagerCreated"),
    // OFFICE_MANAGER_DELETED("OfficeManagerDeleted"),
    // CONTACT_CREATED("ContactCreated"),
    // CONTACT_DELETED("ContactDeleted"),
    // CONTACT_OFFICE_CREATED("ContactOfficeCreated"),
    // CONTACT_OFFICE_DELETED("ContactOfficeDeleted"),
    // SUBJECT_CREATED("SubjectCreated"),
    // SUBJECT_UPDATED("SubjectUpdated"),
    // SUBJECT_CATEGORY_CREATED("SubjectCategoryCreated"),
    // OFFICE_MANAGER_OFFICE_CREATED("OfficeManagerOfficeCreated"),
    // OFFICE_MANAGER_OFFICE_DELETED("OfficeManagerOfficeDeleted"),
    // CONTACT_OFFICE_SUBJECT_CREATED("ContactOfficeSubjectCreated"),
    // CONTACT_OFFICE_SUBJECT_DELETED("ContactOfficeSubjectDeleted"),
    // AFSPRAAK_AANGEMAAKT("AfspraakAangemaakt", false),
    // CUSTOMER_CREATED("CustomerCreated", false),
    // APPOINTMENT_CREATED("AppointmentCreated", false),
    // STARTUP_CREATIE("StartupCreatie", false);

    WEBHOOK_V1("webhook_v1"),
    WEBHOOK_V2("webhook_v2");

    private final String naam;

    MessageType(String naam) {
        this.naam = naam;
    }

    public String getNaam() {
        return naam;
    }

    public static Optional<MessageType> fromNaam(String naam) {
        return stream(MessageType.values()).filter(eventName -> eventName.naam.equalsIgnoreCase(naam)).findFirst();
    }
}
