package org.smartregister.anc.library.util;

/**
 * Created by ndegwamartin on 30/06/2018.
 */
public class DBConstantsUtils {
	public static final String CONTACT_ENTITY_TYPE = "contact";
	public static final String DEMOGRAPHIC_TABLE_NAME = "ec_client";
	public static final String WOMAN_DETAILS_TABLE_NAME = "ec_mother_details";

	public interface RegisterTable {
		String DEMOGRAPHIC = "ec_client";
		String DETAILS = "ec_mother_details";
	}

	public static final class KeyUtils {

		// Base WHO ANC fields
		public static final String ID = "_ID";
		public static final String ID_LOWER_CASE = "_id";
		public static final String STEPNAME = "stepName";
		public static final String NUMBER_PICKER = "number_picker";
		public static final String FIRST_NAME = "first_name";
		public static final String LAST_NAME = "last_name";
		public static final String BASE_ENTITY_ID = "base_entity_id";
		public static final String DOB = "dob";//Date Of Birth
		public static final String DOB_UNKNOWN = "dob_unknown";
		public static final String EDD = "edd";
		public static final String GENDER = "gender";
		public static final String ANC_ID = "register_id";
		public static final String LAST_INTERACTED_WITH = "last_interacted_with";
		public static final String DATE_REMOVED = "date_removed";
		public static final String PHONE_NUMBER = "phone_number";
		public static final String PHONE_NUMBER_REPEAT = "phone_number_repeat";
		public static final String ALT_NAME = "alt_name";
		public static final String ALT_PHONE_NUMBER = "alt_phone_number";
		public static final String HOME_ADDRESS = "home_address";
		public static final String AGE = "age";
		public static final String REMINDERS = "reminders";
		public static final String RED_FLAG_COUNT = "red_flag_count";
		public static final String YELLOW_FLAG_COUNT = "yellow_flag_count";
		public static final String CONTACT_STATUS = "contact_status";
		public static final String PREVIOUS_CONTACT_STATUS = "previous_contact_status";
		public static final String NEXT_CONTACT = "next_contact";
		public static final String NEXT_CONTACT_DATE = "next_contact_date";
		public static final String LAST_CONTACT_RECORD_DATE = "last_contact_record_date";
		public static final String RELATIONAL_ID = "relationalid";
		public static final String VISIT_START_DATE = "visit_start_date";
		public static final String IS_FIRST_VISIT = "is_first_visit";
		public static final String COHABITANTS = "cohabitants";

		// Additional fields for local requirements
		public static final String UID = "uid";
		public static final String UID_REPEAT = "uid_repeat";

		public static final String SSN = "ssn";
		public static final String SSN_REPEAT = "ssn_repeat";

		public static final String ADDITIONAL_ID_TYPE = "additional_id_type";
		public static final String ADDITIONAL_ID_NUMBER = "additional_id_number";
		public static final String ADDITIONAL_ID_NUMBER_REPEAT = "additional_id_number_repeat";

		public static final String SSN_STATUS = "ssn_status";
		public static final String UID_UNKNOWN = "uid_unknown";
		public static final String UID_UNKNOWN_REASON = "uid_unknown_reason";
		public static final String PHONE_NUMBER_UNKNOWN = "phone_number_unknown";
		public static final String PHONE_NUMBER_UNKNOWN_REASON = "phone_number_unknown_reason";
		public static final String LAST_VISIT_DATE = "last_visit_date";

	}
}
