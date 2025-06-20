package com.example.smart_garden.ontology;

import com.example.smart_garden.models.Plant;
import com.example.smart_garden.service.NeedRange;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.OWLEntityRemover;

import java.io.File;
import java.util.*;

public class PlantOntology {

    private OWLOntologyManager ontoManager;
    private OWLOntology plantOntology;
    private OWLDataFactory dataFactory;
    private String ontologyIRIStr;
    private OWLReasoner reasoner;

    public PlantOntology() {
        ontoManager = OWLManager.createOWLOntologyManager();
        dataFactory = ontoManager.getOWLDataFactory();
        loadOntology();
        ontologyIRIStr = plantOntology.getOntologyID().getOntologyIRI().get().toString() + "#";
        OWLReasonerFactory reasonerFactory = new ReasonerFactory();
        reasoner = reasonerFactory.createReasoner(plantOntology);
    }

    private void loadOntology() {
        File ontology = new File("src/main/resources/plant_ontology.owl");
        try {
            plantOntology = ontoManager.loadOntologyFromOntologyDocument(ontology);
        } catch (OWLOntologyCreationException e) {
            System.out.println(e.getMessage());
        }
    }

    private String getClassFriendlyName(OWLClassExpression expr) {
        if (!expr.isAnonymous()) {
            String iri = expr.asOWLClass().getIRI().toString();
            return iri.substring(iri.indexOf("#") + 1);
        }
        return expr.toString();
    }

    private String getIndividualFriendlyName(IRI iri) {
        String iriStr = iri.toString();
        return iriStr.substring(iriStr.indexOf("#") + 1);
    }

    public void createPlantIndividual(Plant plant) {
        String familyName = plant.getFamily();  // ВЗИМАМЕ СЕМЕЙСТВОТО ОТ МОДЕЛА

        OWLNamedIndividual plantIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + plant.getName()));
        OWLClass plantType = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + plant.getType()));
        OWLClass familyClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + familyName));

        // Добавяне на класове, ако не съществуват
        if (!plantOntology.containsClassInSignature(plantType.getIRI())) {
            OWLDeclarationAxiom declareClass = dataFactory.getOWLDeclarationAxiom(plantType);
            ontoManager.applyChange(new AddAxiom(plantOntology, declareClass));
        }

        if (!plantOntology.containsClassInSignature(familyClass.getIRI())) {
            OWLDeclarationAxiom declareFamilyClass = dataFactory.getOWLDeclarationAxiom(familyClass);
            ontoManager.applyChange(new AddAxiom(plantOntology, declareFamilyClass));
        }

        // Установяване на йерархията: plantType SubClassOf familyClass
        OWLSubClassOfAxiom hierarchyAxiom = dataFactory.getOWLSubClassOfAxiom(plantType, familyClass);
        if (!plantOntology.containsAxiom(hierarchyAxiom)) {
            ontoManager.applyChange(new AddAxiom(plantOntology, hierarchyAxiom));
        }

        // Индивид от тип растение
        OWLClassAssertionAxiom axPlant = dataFactory.getOWLClassAssertionAxiom(plantType, plantIndiv);
        ontoManager.applyChange(new AddAxiom(plantOntology, axPlant));
    }


    public void createSymptomForPlant(String plantName, String symptomName) {
        OWLNamedIndividual plantIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + plantName));
        String symptomIndivName = plantName + "_" + symptomName;

        System.out.println("Добавям симптом в онтологията: " + symptomIndivName + " от клас " + symptomName);

        OWLNamedIndividual symptomIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + symptomIndivName));
        OWLClass symptomClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + symptomName));

        // Ако класът на симптома не съществува, го добавяме
        if (!plantOntology.containsClassInSignature(symptomClass.getIRI())) {
            OWLDeclarationAxiom declareSymptomClass = dataFactory.getOWLDeclarationAxiom(symptomClass);
            ontoManager.applyChange(new AddAxiom(plantOntology, declareSymptomClass));
        }

        // Декларираме, че симптомът е от определен клас
        OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(symptomClass, symptomIndiv);
        ontoManager.applyChange(new AddAxiom(plantOntology, classAssertion));

        // Свързваме растението със симптома чрез hasSymptom
        OWLObjectProperty hasSymptom = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasSymptom"));
        OWLObjectPropertyAssertionAxiom link = dataFactory.getOWLObjectPropertyAssertionAxiom(hasSymptom, plantIndiv, symptomIndiv);
        ontoManager.applyChange(new AddAxiom(plantOntology, link));

        // Презареждаме reasoner-а, за да вземе предвид новите факти
        reloadReasoner();
    }

    public List<String> getAllPlantTypes() {
        OWLClass plantClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "Plant"));

        Set<OWLClass> families = reasoner.getSubClasses(plantClass, true).getFlattened();
        Set<OWLClass> plantTypes = new HashSet<>();

        for (OWLClass family : families) {
            if (!family.isOWLNothing()) {
                Set<OWLClass> types = reasoner.getSubClasses(family, true).getFlattened();
                for (OWLClass type : types) {
                    if (!type.isOWLNothing()) {
                        plantTypes.add(type);
                    }
                }
            }
        }

        return plantTypes.stream()
                .map(this::getClassFriendlyName)
                .sorted()
                .toList();
    }


    public Map<String, List<String>> getAllSymptomsGroupedByCategory() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        OWLClass baseSymptomClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "Symptom"));

        Set<OWLClass> allSymptoms = reasoner.getSubClasses(baseSymptomClass, false).getFlattened();

        for (OWLClass parentCategory : allSymptoms) {
            if (parentCategory.isOWLNothing()) continue;

            Set<OWLClass> subSymptoms = reasoner.getSubClasses(parentCategory, false).getFlattened();

            for (OWLClass subCategory : subSymptoms) {
                if (subCategory.isOWLNothing()) continue;

                Set<OWLClass> leafSymptoms = reasoner.getSubClasses(subCategory, true).getFlattened();
                List<String> names = new ArrayList<>();

                for (OWLClass cls : leafSymptoms) {
                    if (!cls.isOWLNothing()) {
                        names.add(getClassFriendlyName(cls));
                    }
                }

                if (!names.isEmpty()) {
                    String label = getClassFriendlyName(parentCategory) + " / " + getClassFriendlyName(subCategory);
                    result.put(label, names);
                }
            }

            // Ако няма подкатегории, взимаме директно "крайните"
            Set<OWLClass> directLeafs = reasoner.getSubClasses(parentCategory, true).getFlattened();
            List<String> directNames = new ArrayList<>();

            for (OWLClass cls : directLeafs) {
                if (!cls.isOWLNothing() && !result.containsKey(getClassFriendlyName(cls))) {
                    directNames.add(getClassFriendlyName(cls));
                }
            }

            if (!directNames.isEmpty()) {
                result.put(getClassFriendlyName(parentCategory), directNames);
            }
        }

        return result;
    }



    public List<String> suggestPlantsFromSameFamily(String plantTypeName) {
        List<String> suggestions = new ArrayList<>();

        // Намиране на класа на растението
        OWLClass targetPlantClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + plantTypeName));

        // Извличане на родителския клас (семейство) чрез reasoner
        Set<OWLClass> superClasses = reasoner.getSuperClasses(targetPlantClass, true).getFlattened();

        for (OWLClass familyClass : superClasses) {
            if (!familyClass.isOWLNothing()) {
                // Взимаме всички под-класове на същото семейство (растения)
                Set<OWLClass> siblingPlants = reasoner.getSubClasses(familyClass, true).getFlattened();
                for (OWLClass sibling : siblingPlants) {
                    String siblingName = getClassFriendlyName(sibling);
                    if (!siblingName.equals(plantTypeName) && !sibling.isOWLNothing()) {
                        suggestions.add(siblingName);
                    }
                }
            }
        }

        return suggestions;
    }

    public List<Map<String, String>> getAllPlantTypesWithFamilies() {
        List<Map<String, String>> result = new ArrayList<>();

        OWLClass plantClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "Plant"));
        Set<OWLClass> families = reasoner.getSubClasses(plantClass, true).getFlattened();

        for (OWLClass familyClass : families) {
            if (familyClass.isOWLNothing()) continue;
            String familyName = getClassFriendlyName(familyClass);
            System.out.println(" -> Семейство: " + familyName);

            Set<OWLClass> types = reasoner.getSubClasses(familyClass, true).getFlattened();
            for (OWLClass typeClass : types) {
                if (typeClass.isOWLNothing()) continue;
                String typeName = getClassFriendlyName(typeClass);
                System.out.println("    -> Тип: " + typeName);

                Map<String, String> entry = new HashMap<>();
                entry.put("type", typeName);
                entry.put("family", familyName);
                result.add(entry);
            }
        }

        return result;
    }





    public List<String> getAdviceForPlantIndividual(String plantIndivName) {
        List<String> result = new ArrayList<>();
        OWLNamedIndividual plantIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + plantIndivName));
        OWLObjectProperty hasSymptom = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasSymptom"));

        for (OWLObjectPropertyAssertionAxiom ax : plantOntology.getObjectPropertyAssertionAxioms(plantIndiv)) {
            if (ax.getProperty().equals(hasSymptom)) {
                OWLNamedIndividual symptomIndiv = ax.getObject().asOWLNamedIndividual();
                String symptomName = getIndividualFriendlyName(symptomIndiv.getIRI());
                System.out.println("SSSSSSSSSSSSSSSSS" + symptomName);
                result.add("Симптом: " + symptomName);

                // Reason за типа на симптома
                Set<OWLClass> symptomTypes = reasoner.getTypes(symptomIndiv, true).getFlattened();
                System.out.println("Типове: " + symptomTypes);
                for (OWLClass symptomClass : symptomTypes) {
                    for (OWLSubClassOfAxiom sa : plantOntology.getSubClassAxiomsForSubClass(symptomClass)) {
                        if (sa.getSuperClass() instanceof OWLObjectSomeValuesFrom) {
                            OWLObjectSomeValuesFrom restriction = (OWLObjectSomeValuesFrom) sa.getSuperClass();
                            OWLObjectPropertyExpression prop = restriction.getProperty();
                            OWLClassExpression filler = restriction.getFiller();

                            if (!filler.isAnonymous()) {
                                OWLClass causeClass = filler.asOWLClass();
                                String causeName = getClassFriendlyName(causeClass);

                                String propName = getClassFriendlyName(
                                        dataFactory.getOWLClass(prop.asOWLObjectProperty().getIRI())
                                );

                                if (propName.toLowerCase().contains("cause")) {
                                    result.add("Възможна причина: " + causeName);

                                    // Потърси care action за причината
                                    for (OWLSubClassOfAxiom ca : plantOntology.getSubClassAxiomsForSubClass(causeClass)) {
                                        if (ca.getSuperClass() instanceof OWLObjectSomeValuesFrom) {
                                            OWLObjectSomeValuesFrom actionRestriction = (OWLObjectSomeValuesFrom) ca.getSuperClass();
                                            OWLObjectProperty actionProp = actionRestriction.getProperty().asOWLObjectProperty();
                                            OWLClassExpression actionExpr = actionRestriction.getFiller();

                                            if (!actionExpr.isAnonymous()) {
                                                OWLClass actionClass = actionExpr.asOWLClass();
                                                String actionClassName = getClassFriendlyName(actionClass);

                                                boolean foundActionIndividual = false;

                                                for (OWLNamedIndividual actionIndiv : plantOntology.getIndividualsInSignature()) {
                                                    Set<OWLClass> actionTypes = reasoner.getTypes(actionIndiv, true).getFlattened();
                                                    for (OWLClass type : actionTypes) {
                                                        if (type.equals(actionClass)) {
                                                            String actionName = getIndividualFriendlyName(actionIndiv.getIRI());
                                                            String actionPropName = getClassFriendlyName(
                                                                    dataFactory.getOWLClass(actionProp.getIRI())
                                                            );

                                                            if (actionPropName.toLowerCase().contains("care")
                                                                    || actionPropName.toLowerCase().contains("treat")) {
                                                                result.add("Препоръчано действие: " + actionName);
                                                                foundActionIndividual = true;
                                                            }
                                                        }
                                                    }
                                                }

                                                if (!foundActionIndividual) {
                                                    result.add("Препоръчано действие (клас): " + actionClassName);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }



    public void removePlantAndSymptoms(String plantName, List<String> symptomNames) {
        OWLEntityRemover remover = new OWLEntityRemover(plantOntology.getImportsClosure());

        OWLNamedIndividual plantIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + plantName));
        plantIndiv.accept(remover);

        if (symptomNames != null) {
            for (String symptom : symptomNames) {
                String symptomIndivName = plantName + "_" + symptom;
                OWLNamedIndividual symptomIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + symptomIndivName));
                symptomIndiv.accept(remover);
            }
        }

        ontoManager.applyChanges(remover.getChanges());
    }

    public Map<String, String> getNeedsFromPlantType(String typeName) {
        Map<String, String> result = new LinkedHashMap<>();

        OWLClass plantClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + typeName));
        OWLObjectProperty hasNeed = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasNeed"));

        // Събиране на всички надкласове включително самия клас
        Set<OWLClass> hierarchy = reasoner.getSuperClasses(plantClass, false).getFlattened();
        hierarchy.add(plantClass);

        for (OWLClass currentClass : hierarchy) {
            for (OWLSubClassOfAxiom ax : plantOntology.getSubClassAxiomsForSubClass(currentClass)) {
                if (ax.getSuperClass() instanceof OWLObjectExactCardinality restriction) {
                    if (restriction.getProperty().asOWLObjectProperty().equals(hasNeed)) {
                        OWLClassExpression filler = restriction.getFiller();

                        if (!filler.isAnonymous()) {
                            String needName = getClassFriendlyName(filler.asOWLClass());

                            if (needName.contains("Water"))
                                result.put("soilMoisture", needName.contains("Frequent") ? "high" : "low");

                            if (needName.contains("Humidity"))
                                result.put("humidity", needName.contains("High") ? "high" : "low");

                            if (needName.contains("Light"))
                                result.put("light", needName.contains("High") ? "high" : "low");

                            if (needName.contains("Temperature"))
                                result.put("temperature", needName.contains("High") ? "high" : "low");
                        }
                    }
                }
            }
        }

        return result;
    }




    public Plant getPlantByIndividualName(String plantIndivName) {
        Plant plantModel = new Plant();
        plantModel.setName(plantIndivName);

        List<String> symptoms = new ArrayList<>();
        OWLNamedIndividual plantIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + plantIndivName));

        Set<OWLClass> types = reasoner.getTypes(plantIndiv, true).getFlattened();
        for (OWLClass plantClass : types) {
            plantModel.setType(getClassFriendlyName(plantClass));
        }

        OWLObjectProperty hasSymptom = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasSymptom"));
        for (OWLObjectPropertyAssertionAxiom ax : plantOntology.getObjectPropertyAssertionAxioms(plantIndiv)) {
            if (ax.getProperty().equals(hasSymptom)) {
                OWLNamedIndividual symptom = ax.getObject().asOWLNamedIndividual();
                symptoms.add(getIndividualFriendlyName(symptom.getIRI()));
            }
        }
        plantModel.setSymptoms(symptoms);
        return plantModel;
    }

    public void reloadReasoner() {
        OWLReasonerFactory reasonerFactory = new ReasonerFactory();
        this.reasoner = reasonerFactory.createReasoner(plantOntology);
    }

    public void saveOntology() {
        try {
            ontoManager.saveOntology(plantOntology);
        } catch (OWLOntologyStorageException e) {
            System.out.println("Error saving ontology: " + e.getMessage());
        }


    }
}
