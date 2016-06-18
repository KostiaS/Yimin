package immigration.gen;

import immigration.dao.*;
import immigration.interfaces.IGenerator;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by UA C on 14.06.2016.
 */
public class GeneratorNormal {

    private static final int STEP_AMOUNT = 5;//5
    private static final int COUNTYES_AMOUNT = 5;//52
    private static final int EMBASSYES_AMOUNT = 5;//400
    private static final int PROGRAMS_AMOUNT = 5;//50 shoud be less than country
    private static final int MAX_PROGRAMSTEP = 10;//10 value should be bigger or equals STEP_AMOUNT
    //private static final int DOCUMENTS_AMOUNT = 3;//150
    private static final int FIELDNAMES_AMOUNT = 10;//20
    private static final String PHOTO_FILE_PATH = "../Yimin/resources/doc.jpg";
    private static final String PRS_DOC_FILE_PATH = "../Yimin/resources/passport.jpg";
    private static final String MASK_FILE_PATH = "../Yimin/resources/mask.html";


    int city = 1;
    int region = 1;
    int street = 1;
    int countPersons = 1;
    int firstNameCounter = 0;
    int lastnameCounter = 0;
    int personDataCounter = 0;
    int count = 1;

    @PersistenceContext(unitName = "springHibernate", type = PersistenceContextType.EXTENDED)
    EntityManager em;

    // interface that implement methods wich generate values
    IGenerator ig;

    public void setIg(IGenerator ig) {
        this.ig = ig;
    }

    @Transactional
    public void generateFieldNamesList() {
        for (int i = 1; i < FIELDNAMES_AMOUNT; i++) {
            FieldNames fieldNames = new FieldNames();
            fieldNames.setName(ig.randomString("fieldName", i));
            fieldNames.setPossibleValues(ig.randomString("posibleValueJson", i));
            em.persist(fieldNames);
        }
        generatePersonCustomData();
        generateProgramCustomData();
    }

    @Transactional
    private void generateProgramCustomData() {
        for (int i = 1; i < PROGRAMS_AMOUNT + 1; i++) {
            ProgramCustomData programCustomData = new ProgramCustomData();
            programCustomData.setValue(ig.randomString("value", i));
            programCustomData.setProgram(getRandomObjectFromDb(new Programs()));
            programCustomData.setFieldNames(getRandomObjectFromDb(new FieldNames()));
            em.persist(programCustomData);
        }

    }

    @SuppressWarnings("unchecked")
    public <T> T getRandomObjectFromDb(T randomObject) {
        String className = randomObject.getClass().getSimpleName();
        int maxIdValue = 0;
        Query q = em.createQuery("SELECT p FROM " + className + " p WHERE p.id = ?1");
        maxIdValue = em.createQuery("select max(u.id) from " + className + " u", Integer.class).getSingleResult();
        q.setParameter(1, ig.randBetween(1, maxIdValue));
        randomObject = null;
        try {
            randomObject = (T) q.getSingleResult();
        } catch (Exception e) {
            System.out.println("em not found object " + className + " in DB" + "[ max value" + maxIdValue + "]");
        }
        return randomObject;
    }

    @SuppressWarnings("unchecked")
    public <T> T getObjectFromDbById(Class<T> nameOfClass, int id) {
        String className = nameOfClass.getSimpleName();
        Query q = em.createQuery("SELECT p FROM " + className + " p WHERE p.id = ?1");
        q.setParameter(1, id);
        T randomObject = null;
        try {
            randomObject = (T) q.getSingleResult();
        } catch (Exception e) {
            System.out.println("em not found object " + className + " in DB and id = " + id);
        }
        return randomObject;

    }

    @Transactional
    private void generatePersonCustomData() {
        for (int i = 1; i < personDataCounter - 2; i++) {
            for (int y = 0; y < 5; y++) {
                PersonCustomData personCustomData = new PersonCustomData();
                personCustomData.setPersonData(getObjectFromDbById(PersonData.class, i));
                personCustomData.setValue(ig.randomString("some value", i));
                personCustomData.setFieldNames(getRandomObjectFromDb(new FieldNames()));
                em.persist(personCustomData);
            }
        }
    }

    @Transactional
    public void generateCoutryList() {
        for (int i = 1; i < COUNTYES_AMOUNT; i++) {
            Country country = new Country();
            country.setLink(ig.randomString("www.coutry", i) + ".gov");
            country.setName(ig.randomString("Country", i));
            em.persist(country);
            em.clear();
        }
        System.out.println("COUNTRY LIST PERSISTED");
    }

    @Transactional
    public void generateDocumentsList() {
        for (int i = 1; i < PROGRAMS_AMOUNT + 1; i++) {
            for (int x = 0; x < 3; x++) {
                Documents documents = new Documents();
                documents.setFile(generateBlobFromFile(PHOTO_FILE_PATH));
                documents.setMask(generateBlobFromFile(MASK_FILE_PATH));
                documents.setNameOfFile("doc.jpg");
                documents.setImage(ig.randomString("www.photoRepository" + x, i) + ".com");
                documents.setType(ig.statusGenerator(5, "Type"));
                documents.setNameOfDocument(ig.statusGenerator(10, "document name"));
                documents.setProg(getObjectFromDbById(Programs.class, i));
                documents.setDocumentField(generateDocumentFieldList());
                em.persist(documents);
                em.flush();
                em.clear();
            }
        }
        System.out.println("DOC LIST PERSISTED");
    }

    @Transactional
    private Blob generateBlobFromFile(String path) {
        Session session = em.unwrap(Session.class);
        File file = new File(path);
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage() + "cant find file");
        }
        Blob blob = Hibernate.getLobCreator(session)
                .createBlob(inputStream, file.length());
        return blob;
    }

    @Transactional
    private List<DocumentField> generateDocumentFieldList() {
        List<DocumentField> docFieldList = new ArrayList<>();
        for (int i = 0; i < ig.randBetween(3, 15); i++) {
            DocumentField documentField = new DocumentField();
            documentField.setAttribute(ig.randomString("attribute", i));
            documentField.setFlagPersonData(ig.booleanGenerator());
            documentField.setName(ig.statusGenerator(10, "someName"));

            em.persist(documentField);
            docFieldList.add(documentField);
        }
        return docFieldList;
    }

    @Transactional
    public void generateProgramsList() {
        for (int i = 1; i < COUNTYES_AMOUNT; i++) {
            for (int y = 0; y < 3; y++) {
                Programs programs = new Programs();
                programs.setCategory(ig.randomString("category", i));
                programs.setCountry(getObjectFromDbById(Country.class, i));
                programs.setDescription(ig.randomString("description", i));
                programs.setEnabled(ig.booleanGenerator());
                programs.setLink(ig.randomString("www.programs", i) + ".com");
                programs.setModified(ig.getRandomData());
                programs.setName(ig.randomString("nameOfPrograms", i));
                programs.setStartProgram(ig.getRandomData());

                em.persist(programs);

                em.clear();
            }
        }
        System.out.println("PROGRAMS LIST PERSISTED");
        generateSteps();
        System.out.println("STEPS LIST PERSISTED");
        generateProgramStep();
        System.out.println("PROGRAM STEPS LIST PERSISTED");
    }

    private void generateProgramStep() {
        int programsCount = 1;
        for (int y = 0; y < PROGRAMS_AMOUNT; y++) {
            programsCount++;
            for (int i = 1; i < ig.randBetween(5, MAX_PROGRAMSTEP); i++) {
                ProgramStep programStep = new ProgramStep();
                programStep.setDescription(ig.randomString("description", i));
                programStep.setFileName(ig.randomString("fileName", i));
                programStep.setProgram(getObjectFromDbById(Programs.class, programsCount));
                programStep.setStep(getRandomObjectFromDb(new Step()));
                programStep.setStepOrder(i);
                em.persist(programStep);
            }
        }
        em.clear();
    }

    @Transactional
    private void generateSteps() {

        for (int i = 1; i < STEP_AMOUNT; i++) {
            Step step = new Step();
            step.setDescription(ig.randomString("description", i));
            step.setName(ig.randomString("name", i));
            em.persist(step);

        }

    }

    @Transactional
    public void generateEmbassyList() {
        for (int i = 1; i < EMBASSYES_AMOUNT; i++) {
            Embassy embassy = new Embassy();
            embassy.setPhone(ig.phoneGenerator());
            embassy.setFax(ig.phoneGenerator());
            embassy.setEmail(ig.randomEmail(i));
            embassy.setType(ig.statusGenerator(20, "type"));
            embassy.setLink(ig.randomString("www.Embasy", i) + ".gov");
            embassy.setAddress(ig.randomString("addressOfEmbassy", i));
            embassy.setCountry(getRandomObjectFromDb(new Country()));
            embassy.setLocation(getRandomObjectFromDb(new Country()));

            em.persist(embassy);
            em.clear();

        }
        System.out.println("EMBASSY LIST PERSISTED");
    }

    @Transactional
    public void fillDb(int i) {

        addPerson();

    }

    @Transactional
    public void addPerson() {

        PersonData persondata = generatePersondata(personDataCounter++);
        Person person = new Person();
        person.setEmail(ig.randomEmail(countPersons));
        person.setPassword(ig.randomString("password", countPersons));
        person.setRegistration(ig.getRandomData());
        person.setLastLogin(ig.getRandomData());
        person.setPersonData(persondata);

        em.persist(person);
        em.flush();


        em.clear();
        //generateListOfFemiliMembers2();
        System.out.println("Person persisted" + countPersons);
        countPersons++;
    }

    @Transactional
    private void generateListOfFemiliMembers2() {

        for (int i = 0; i < ig.randBetween(0, 4); i++) {
            PersonData familiMemberData = generatePersondata(personDataCounter++);
            FamilyMember fmem = new FamilyMember();
            fmem.setPersonData(familiMemberData);
            fmem.setRelation(ig.statusGenerator(5, "relation"));
            fmem.setPerson(getRandomObjectFromDb(new Person()));
            em.persist(fmem);

        }

    }

    @Transactional
    public PersonData generatePersondata(int personId) {

        PersonData personData = new PersonData();

        personData.setIdentify("" + ig.randBetween(10000000, Integer.MAX_VALUE));
        personData.setBirthdate(ig.getRandomData());
        personData.setFirstName(ig.randomString("firstname", firstNameCounter++));
        personData.setLastName(ig.randomString("lastname", lastnameCounter++));
        personData.setGender(ig.randomGender());
        personData.setFamilyStatus(ig.statusGenerator(5, "familyStatus"));
        personData.setWorkphone(ig.phoneGenerator());
        personData.setMobilephone(ig.phoneGenerator());
        personData.setHomephone(ig.phoneGenerator());
        personData.setOcupation(ig.randomString("occupation", personId));
        personData.setEducation(ig.randomString("education", personId));
        personData.setBirthplace(getRandomObjectFromDb(new Country()));
        personData.setCitizenship(generateCountryesListForPersonData(personData.getBirthplace()));

        em.persist(personData);
        em.flush();
        em.clear();
        generateAddresses();
        generatePersonDocuments();
        return personData;
    }

    @Transactional
    private void generatePersonDocuments() {

        PersonDocuments personDocuments = new PersonDocuments();
        personDocuments.setDocumentType(ig.statusGenerator(10, "Type"));
        personDocuments.setExpirationDate(ig.getRandomData());
        personDocuments.setLanguage(ig.randomString("language", 0));
        personDocuments.setDocument(generateBlobFromFile(PRS_DOC_FILE_PATH));
        personDocuments.setTemporary(ig.booleanGenerator());
        personDocuments.setTranslation(ig.randomString("translation", 0));
        personDocuments.setPersonData(getRandomObjectFromDb(new PersonData()));
        em.persist(personDocuments);
        em.clear();
    }

    private List<Country> generateCountryesListForPersonData(Country birthPlace) {
        List<Country> randomCountryList = new ArrayList<>();
        randomCountryList.add(birthPlace);
        for (int i = 0; i < ig.randBetween(0, 1); i++) {
            randomCountryList.add(getRandomObjectFromDb(new Country()));

        }
        return randomCountryList;
    }

    @Transactional
    private void generateAddresses() {
        for (int i = 0; i < ig.randBetween(0, 3); i++) {
            Address address = new Address();
            address.setAprt(ig.randBetween(0, 500));
            address.setCity(ig.randomString("City", city++));
            address.setCountry(getRandomObjectFromDb(new Country()));
            address.setMainAddress(ig.booleanGenerator());
            address.setRegion(ig.randomString("Region", region++));
            address.setStreet(ig.randomString("Street", street++));
            address.setPersonData(getRandomObjectFromDb(new PersonData()));
            address.setBld(ig.randomString("Building", ig.randBetween(0, 150)));
            address.setEmb(getRandomObjectFromDb(new Embassy()));

            em.persist(address);
            em.clear();
        }
    }

}