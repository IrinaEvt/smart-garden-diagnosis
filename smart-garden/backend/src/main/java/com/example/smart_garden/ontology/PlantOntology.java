package com.example.smart_garden.ontology;

import com.example.smart_garden.entities.SymptomEntity;
import com.example.smart_garden.models.Plant;
import com.example.smart_garden.service.NeedRange;
import com.example.smart_garden.service.ReasoningBlock;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class PlantOntology {

    private OWLOntologyManager ontoManager;
    private OWLOntology plantOntology;
    private OWLDataFactory dataFactory;
    private String ontologyIRIStr;
  //  private OWLReasoner reasoner;

    public PlantOntology() {
        ontoManager = OWLManager.createOWLOntologyManager();
        dataFactory = ontoManager.getOWLDataFactory();
        loadOntology();
        ontologyIRIStr = plantOntology.getOntologyID().getOntologyIRI().get().toString() + "#";
        OWLReasonerFactory reasonerFactory = new ReasonerFactory();
    //    reasoner = reasonerFactory.createReasoner(plantOntology);
    }

    private void loadOntology() {
        File ontology = new File("src/main/resources/plant_ontology.owl");
        try {
            plantOntology = ontoManager.loadOntologyFromOntologyDocument(ontology);
        } catch (OWLOntologyCreationException e) {
            System.out.println(e.getMessage());
        }
    }

    private OWLReasoner createReasoner() {
        OWLReasonerFactory reasonerFactory = new ReasonerFactory();
        return reasonerFactory.createReasoner(plantOntology);
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


        if (!plantOntology.containsClassInSignature(plantType.getIRI())) {
            OWLDeclarationAxiom declareClass = dataFactory.getOWLDeclarationAxiom(plantType);
            ontoManager.applyChange(new AddAxiom(plantOntology, declareClass));
        }

        if (!plantOntology.containsClassInSignature(familyClass.getIRI())) {
            OWLDeclarationAxiom declareFamilyClass = dataFactory.getOWLDeclarationAxiom(familyClass);
            ontoManager.applyChange(new AddAxiom(plantOntology, declareFamilyClass));
        }




        OWLSubClassOfAxiom hierarchyAxiom = dataFactory.getOWLSubClassOfAxiom(plantType, familyClass);
        if (!plantOntology.containsAxiom(hierarchyAxiom)) {
            ontoManager.applyChange(new AddAxiom(plantOntology, hierarchyAxiom));
        }


        OWLClassAssertionAxiom axPlant = dataFactory.getOWLClassAssertionAxiom(plantType, plantIndiv);
        ontoManager.applyChange(new AddAxiom(plantOntology, axPlant));
    }

    private <T> T withReasoner(Function<OWLReasoner, T> action) {
        OWLReasoner reasoner = reloadReasoner();
        try {
            return action.apply(reasoner);
        } finally {
            reasoner.dispose();
        }
    }


    public void createSymptomForPlant(String plantName, String symptomName) {
        OWLNamedIndividual plantIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + plantName));
        String symptomIndivName = plantName + "_" + symptomName;

        System.out.println("Добавям симптом в онтологията: " + symptomIndivName + " от клас " + symptomName);

        OWLNamedIndividual symptomIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + symptomIndivName));
        OWLClass symptomClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + symptomName));


        if (!plantOntology.containsClassInSignature(symptomClass.getIRI())) {
            OWLDeclarationAxiom declareSymptomClass = dataFactory.getOWLDeclarationAxiom(symptomClass);
            ontoManager.applyChange(new AddAxiom(plantOntology, declareSymptomClass));
        }


        OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(symptomClass, symptomIndiv);
        ontoManager.applyChange(new AddAxiom(plantOntology, classAssertion));


        OWLObjectProperty hasSymptom = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasSymptom"));
        OWLObjectPropertyAssertionAxiom link = dataFactory.getOWLObjectPropertyAssertionAxiom(hasSymptom, plantIndiv, symptomIndiv);
        ontoManager.applyChange(new AddAxiom(plantOntology, link));


        reloadReasoner();
    }

    public List<String> getAllPlantTypes() {
        OWLClass plantClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "Plant"));
        return withReasoner(reasoner -> {
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
        });
    }

    public List<String> getAllLeafSymptoms() {
        List<String> leafSymptoms = new ArrayList<>();
        OWLClass baseSymptomClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "Symptom"));

        withReasoner(reasoner -> {
            Set<OWLClass> allSymptoms = reasoner.getSubClasses(baseSymptomClass, false).getFlattened();

            for (OWLClass symptomClass : allSymptoms) {
                if (symptomClass.isOWLNothing()) continue;

                Set<OWLClass> subClasses = reasoner.getSubClasses(symptomClass, true).getFlattened();
                boolean isLeaf = subClasses.stream().allMatch(OWLClass::isOWLNothing);

                if (isLeaf) {
                    leafSymptoms.add(getClassFriendlyName(symptomClass));
                }
            }
            return null;
        });

        return leafSymptoms.stream().sorted().toList();
    }

    public Map<String, List<String>> getAllSymptomsGroupedByCategory() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        OWLClass baseSymptomClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "Symptom"));

        withReasoner(reasoner -> {
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
            return null;
        });

        return result;
    }





    public List<String> suggestPlantsFromSameFamily(String plantTypeName) {
        OWLClass targetPlantClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + plantTypeName));
        return withReasoner(reasoner -> {
            List<String> suggestions = new ArrayList<>();
            Set<OWLClass> superClasses = reasoner.getSuperClasses(targetPlantClass, true).getFlattened();

            for (OWLClass familyClass : superClasses) {
                if (!familyClass.isOWLNothing()) {
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
        });
    }

    public List<Map<String, String>> getAllPlantTypesWithFamilies() {
        OWLClass plantClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "Plant"));
        return withReasoner(reasoner -> {
            List<Map<String, String>> result = new ArrayList<>();
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
        });
    }

    public List<ReasoningBlock> getAdviceForPlantIndividual(String plantIndivName) {
        OWLNamedIndividual plantIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + plantIndivName));
        OWLObjectProperty hasSymptom = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasSymptom"));

        return withReasoner(reasoner -> {
            List<ReasoningBlock> grouped = new ArrayList<>();
            Map<String, ReasoningBlock> map = new LinkedHashMap<>();

            for (OWLObjectPropertyAssertionAxiom ax : plantOntology.getObjectPropertyAssertionAxioms(plantIndiv)) {
                if (ax.getProperty().equals(hasSymptom)) {
                    OWLNamedIndividual symptomIndiv = ax.getObject().asOWLNamedIndividual();
                    String symptomName = getIndividualFriendlyName(symptomIndiv.getIRI());

                    Set<OWLClass> symptomTypes = reasoner.getTypes(symptomIndiv, true).getFlattened();
                    for (OWLClass symptomClass : symptomTypes) {
                        for (OWLSubClassOfAxiom sa : plantOntology.getSubClassAxiomsForSubClass(symptomClass)) {
                            if (sa.getSuperClass() instanceof OWLObjectSomeValuesFrom restriction) {
                                OWLObjectPropertyExpression prop = restriction.getProperty();
                                OWLClassExpression filler = restriction.getFiller();

                                if (!filler.isAnonymous()) {
                                    OWLClass causeClass = filler.asOWLClass();
                                    String causeName = getClassFriendlyName(causeClass);
                                    String propName = getClassFriendlyName(
                                            dataFactory.getOWLClass(prop.asOWLObjectProperty().getIRI())
                                    );

                                    if (propName.toLowerCase().contains("cause")) {
                                        ReasoningBlock block = map.computeIfAbsent(causeName, k -> {
                                            ReasoningBlock b = new ReasoningBlock();
                                            b.setCause(causeName);
                                            return b;
                                        });

                                        List<String> symptoms = block.getSymptoms();
                                        symptoms.add(symptomName);
                                        block.setSymptoms(symptoms);

                                        for (OWLSubClassOfAxiom ca : plantOntology.getSubClassAxiomsForSubClass(causeClass)) {
                                            if (ca.getSuperClass() instanceof OWLObjectSomeValuesFrom actionRestriction) {
                                                OWLObjectProperty actionProp = actionRestriction.getProperty().asOWLObjectProperty();
                                                OWLClassExpression actionExpr = actionRestriction.getFiller();

                                                if (!actionExpr.isAnonymous()) {
                                                    OWLClass actionClass = actionExpr.asOWLClass();
                                                    String actionClassName = getClassFriendlyName(actionClass);
                                                    boolean found = false;
                                                    List<String> actions = block.getActions();
                                                    for (OWLNamedIndividual actionIndiv : plantOntology.getIndividualsInSignature()) {
                                                        Set<OWLClass> types = reasoner.getTypes(actionIndiv, true).getFlattened();
                                                        for (OWLClass t : types) {
                                                            if (t.equals(actionClass)) {
                                                                String name = getIndividualFriendlyName(actionIndiv.getIRI());
                                                                String actionPropName = getClassFriendlyName(
                                                                        dataFactory.getOWLClass(actionProp.getIRI()));
                                                                if (actionPropName.toLowerCase().contains("care") ||
                                                                        actionPropName.toLowerCase().contains("treat")) {
                                                                    actions.add(name);
                                                                    found = true;
                                                                }
                                                            }
                                                        }
                                                    }
                                                    block.setActions(actions);

                                                    if (!found) {
                                                        actions.add(actionClassName);
                                                        block.setActions(actions);
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
            grouped.addAll(map.values());
            return grouped;
        });
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

    public Optional<String> suggestEasyCarePlant() {
        OWLObjectProperty hasEaseOfCare = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasEaseOfCare"));
        OWLClass easyClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "Easy"));
        OWLClass plantClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "Plant"));

        return withReasoner(reasoner -> {
            Set<OWLClass> plantTypes = reasoner.getSubClasses(plantClass, false).getFlattened();

            for (OWLClass plantType : plantTypes) {
                if (plantType.isOWLNothing()) continue;

                Set<OWLClassExpression> superClasses = new HashSet<>(EntitySearcher.getSuperClasses(plantType, plantOntology));

                for (OWLClassExpression expr : superClasses) {
                    if (expr instanceof OWLObjectSomeValuesFrom someValuesFrom) {
                        if (someValuesFrom.getProperty().equals(hasEaseOfCare)
                                && someValuesFrom.getFiller().equals(easyClass)) {
                            return Optional.of(getClassFriendlyName(plantType));
                        }
                    }

                    if (expr instanceof OWLObjectExactCardinality exact
                            && exact.getProperty().equals(hasEaseOfCare)
                            && exact.getCardinality() == 1
                            && exact.getFiller().equals(easyClass)) {
                        return Optional.of(getClassFriendlyName(plantType));
                    }
                }
            }

            return Optional.empty();
        });
    }

    public Map<String, String> getNeedsFromPlantType(String typeName) {
        Map<String, String> result = new LinkedHashMap<>();

        OWLClass plantClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + typeName));
        OWLObjectProperty hasNeed = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasNeed"));

        return withReasoner(reasoner -> {
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
        });
    }

    public Plant getPlantByIndividualName(String plantIndivName) {
        Plant plantModel = new Plant();
        plantModel.setName(plantIndivName);

        List<String> symptoms = new ArrayList<>();
        OWLNamedIndividual plantIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + plantIndivName));

        return withReasoner(reasoner -> {
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
        });
    }

    public OWLReasoner reloadReasoner() {
        OWLReasonerFactory reasonerFactory = new ReasonerFactory();
        return reasonerFactory.createReasoner(plantOntology);
    }


    public void saveOntology() {
        try {
            ontoManager.saveOntology(plantOntology);
        } catch (OWLOntologyStorageException e) {
            System.out.println("Error saving ontology: " + e.getMessage());
        }
    }

    public void addEnvironmentalCondition(String plantName, String conditionName) {
        OWLNamedIndividual plantIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + plantName));
        String conditionIndivName = plantName + "_" + conditionName;
        OWLNamedIndividual conditionIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + conditionIndivName));

        if (plantOntology.containsIndividualInSignature(conditionIndiv.getIRI())) {
            System.out.println("⚠️ Условие вече съществува и ще бъде пропуснато: " + conditionIndivName);
            return;
        }

        OWLClass conditionClass = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + conditionName));
        OWLClassAssertionAxiom typeAxiom = dataFactory.getOWLClassAssertionAxiom(conditionClass, conditionIndiv);
        ontoManager.applyChange(new AddAxiom(plantOntology, typeAxiom));

        OWLObjectProperty hasCondition = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasCondition"));
        OWLObjectPropertyAssertionAxiom link = dataFactory.getOWLObjectPropertyAssertionAxiom(hasCondition, plantIndiv, conditionIndiv);
        ontoManager.applyChange(new AddAxiom(plantOntology, link));

        System.out.println("➕ Добавено състояние: " + conditionName);
    }



    public void evaluateAndAddRisks(String plantName) {
        reloadReasoner();
        OWLNamedIndividual plantIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + plantName));
        OWLObjectProperty hasCondition = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasCondition"));
        OWLObjectProperty hasRisk = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasRisk"));

        withReasoner(reasoner -> {
            System.out.println("🔍 Индивиди със състояния:");
            for (OWLObjectPropertyAssertionAxiom ax : plantOntology.getObjectPropertyAssertionAxioms(plantIndiv)) {
                if (ax.getProperty().equals(hasCondition)) {
                    OWLNamedIndividual conditionIndiv = ax.getObject().asOWLNamedIndividual();
                    Set<OWLClass> conditionTypes = reasoner.getTypes(conditionIndiv, true).getFlattened();

                    for (OWLClass conditionClass : conditionTypes) {
                        Set<OWLClassExpression> allSupers = new HashSet<>();
                        allSupers.addAll(EntitySearcher.getSuperClasses(conditionClass, plantOntology));
                        allSupers.addAll(EntitySearcher.getEquivalentClasses(conditionClass, plantOntology));

                        for (OWLClassExpression sup : allSupers) {
                            if (sup instanceof OWLObjectSomeValuesFrom some && some.getProperty().equals(hasRisk)) {
                                OWLClass riskClass = some.getFiller().asOWLClass();

                                String riskIndivName = plantName + "_" + riskClass.getIRI().getShortForm();
                                OWLNamedIndividual riskIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + riskIndivName));

                                if (plantOntology.containsIndividualInSignature(riskIndiv.getIRI())) {
                                    System.out.println("⚠️ Риск вече съществува и ще бъде пропуснат: " + riskIndivName);
                                    continue;
                                }

                                OWLClassAssertionAxiom riskTypeAx = dataFactory.getOWLClassAssertionAxiom(riskClass, riskIndiv);
                                ontoManager.applyChange(new AddAxiom(plantOntology, riskTypeAx));

                                OWLObjectPropertyAssertionAxiom link = dataFactory.getOWLObjectPropertyAssertionAxiom(hasRisk, plantIndiv, riskIndiv);
                                ontoManager.applyChange(new AddAxiom(plantOntology, link));

                                System.out.println("⚠️ Добавен риск: " + riskClass.getIRI().getShortForm());
                            }
                        }
                    }
                }
            }
            return null;
        });
    }





    public void evaluateAndAddComplexCondition(String plantName) {
        OWLNamedIndividual plantIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + plantName));
        OWLObjectProperty hasCondition = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasCondition"));

        Set<OWLNamedIndividual> conditions = new HashSet<>();
        for (OWLObjectPropertyAssertionAxiom ax : plantOntology.getObjectPropertyAssertionAxioms(plantIndiv)) {
            if (ax.getProperty().equals(hasCondition)) {
                conditions.add(ax.getObject().asOWLNamedIndividual());
            }
        }

        String ghostName = plantName + "_GhostCondition";
        OWLNamedIndividual ghostIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + ghostName));

        // Изтрий предишен ghost (ако има)
        if (plantOntology.containsIndividualInSignature(ghostIndiv.getIRI())) {
            OWLEntityRemover remover = new OWLEntityRemover(plantOntology.getImportsClosure());
            ghostIndiv.accept(remover);
            ontoManager.applyChanges(remover.getChanges());
        }

        // Добави нов ghostIndiv с връзки
        for (OWLNamedIndividual cond : conditions) {
            OWLObjectPropertyAssertionAxiom link = dataFactory.getOWLObjectPropertyAssertionAxiom(hasCondition, ghostIndiv, cond);
            ontoManager.applyChange(new AddAxiom(plantOntology, link));
        }

        // ✅ reload след добавянето
        reloadReasoner();

        System.out.println("📋 Ghost индивид: " + ghostIndiv.getIRI().getShortForm());
        for (OWLObjectPropertyAssertionAxiom ax : plantOntology.getObjectPropertyAssertionAxioms(ghostIndiv)) {
            if (ax.getProperty().equals(hasCondition)) {
                System.out.println("🔗 Ghost има hasCondition към: " + ax.getObject().asOWLNamedIndividual().getIRI().getShortForm());
            }
        }

        withReasoner(reasoner -> {
            OWLClass complexSuper = dataFactory.getOWLClass(IRI.create(ontologyIRIStr + "ComplexCondition"));
            Set<OWLClass> inferred = reasoner.getTypes(ghostIndiv, true).getFlattened();

            for (OWLClass cls : inferred) {
                if (!cls.isOWLNothing() && reasoner.getSuperClasses(cls, false).containsEntity(complexSuper)) {
                    String inferredName = getClassFriendlyName(cls);
                    String finalIndivName = plantName + "_" + inferredName;

                    OWLNamedIndividual newIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + finalIndivName));
                    if (plantOntology.containsIndividualInSignature(newIndiv.getIRI())) {
                        System.out.println("⚠️ ComplexCondition вече съществува: " + finalIndivName);
                        continue;
                    }

                    OWLClassAssertionAxiom ax = dataFactory.getOWLClassAssertionAxiom(cls, newIndiv);
                    OWLObjectPropertyAssertionAxiom link = dataFactory.getOWLObjectPropertyAssertionAxiom(hasCondition, plantIndiv, newIndiv);

                    ontoManager.applyChange(new AddAxiom(plantOntology, ax));
                    ontoManager.applyChange(new AddAxiom(plantOntology, link));

                    System.out.println("✅ Инфериран ComplexCondition: " + inferredName);
                }
            }
            return null;
        });

        // Изтрий ghost
        OWLEntityRemover remover = new OWLEntityRemover(plantOntology.getImportsClosure());
        ghostIndiv.accept(remover);
        ontoManager.applyChanges(remover.getChanges());
        System.out.println("🧽 Изтрит временен ghost индивид: " + ghostName);
    }






    public void evaluateAndAddCondition(String plantName, String param, double value) {
        String type = getPlantByIndividualName(plantName).getType();
        Map<String, String> needs = getNeedsFromPlantType(type);


        System.out.println("🌿 evaluateAndAddCondition: " + plantName + " | " + param + "=" + value);
        System.out.println("   ➤ Тип: " + type + " | Нужда: " + needs.get(param));


        if (!needs.containsKey(param)) return;

        String needLevel = needs.get(param);
        String condition = null;

        condition = determineCondition(param, needLevel, value);


        if (condition != null) {
            addEnvironmentalCondition(plantName, condition);

        }
    }

    public String determineCondition(String param, String needLevel, double value) {
        switch (param) {
            case "temperature":
                if (needLevel.equals("low") && value > 35) return "HighTemperatureCondition";
                if (needLevel.equals("high") && value < 20) return "LowTemperatureCondition";
                break;
            case "humidity":
                if (needLevel.equals("low") && value > 60) return "HighHumidityCondition";
                if (needLevel.equals("high") && value < 60) return "LowHumidityCondition";
                break;
            case "light":
                if (needLevel.equals("low") && value > 500) return "HighLightCondition";
                if (needLevel.equals("high") && value < 500) return "LowLightCondition";
                break;
            case "soilMoisture":
                if (needLevel.equals("low") && value > 60) return "HighSoilMoistureCondition";
                if (needLevel.equals("high") && value < 40) return "LowSoilMoistureCondition";
                break;
        }
        return null;
    }


    public List<String> getRisksForPlant(String plantName) {
        Set<String> risks = new HashSet<>();

        OWLNamedIndividual plantIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + plantName));
        OWLObjectProperty hasRisk = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasRisk"));

        withReasoner(reasoner -> {
            NodeSet<OWLNamedIndividual> inferred = reasoner.getObjectPropertyValues(plantIndiv, hasRisk);
            for (OWLNamedIndividual riskIndiv : inferred.getFlattened()) {
                Set<OWLClass> types = reasoner.getTypes(riskIndiv, true).getFlattened();
                for (OWLClass cls : types) {
                    if (!cls.isOWLNothing()) {
                        risks.add(getClassFriendlyName(cls));
                    }
                }
            }
            return null;
        });

        return risks.stream().sorted().toList();
    }




    public void clearAllConditionsAndRisks(String plantName) {
        OWLNamedIndividual plantIndiv = dataFactory.getOWLNamedIndividual(IRI.create(ontologyIRIStr + plantName));
        OWLObjectProperty hasCondition = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasCondition"));
        OWLObjectProperty hasRisk = dataFactory.getOWLObjectProperty(IRI.create(ontologyIRIStr + "hasRisk"));

        List<OWLObjectPropertyAssertionAxiom> toRemove = new ArrayList<>();
        Set<OWLAxiom> additionalRemovals = new HashSet<>();

        // Събира всички hasCondition и hasRisk връзки
        for (OWLObjectPropertyAssertionAxiom ax : plantOntology.getObjectPropertyAssertionAxioms(plantIndiv)) {
            if (ax.getProperty().equals(hasCondition) || ax.getProperty().equals(hasRisk)) {
                toRemove.add(ax);

                // Ако сме добавяли и тип за риска (например: riskIndiv rdf:type RiskClass), махаме и него
                OWLNamedIndividual target = ax.getObject().asOWLNamedIndividual();
                for (OWLClassAssertionAxiom ca : plantOntology.getClassAssertionAxioms(target)) {
                    additionalRemovals.add(ca);
                }
            }
        }

        // Премахване
        for (OWLAxiom ax : toRemove) {
            ontoManager.applyChange(new RemoveAxiom(plantOntology, ax));
        }
        for (OWLAxiom ax : additionalRemovals) {
            ontoManager.applyChange(new RemoveAxiom(plantOntology, ax));
        }


        System.out.println("🧹 Изчистени условия и рискове за " + plantName);
    }

    public void evaluatePlantState(String plantName, Map<String, Double> values) {
        clearAllConditionsAndRisks(plantName);

        for (Map.Entry<String, Double> entry : values.entrySet()) {
            evaluateAndAddCondition(plantName, entry.getKey(), entry.getValue());
        }

        evaluateAndAddComplexCondition(plantName);
        evaluateAndAddRisks(plantName);
        reloadReasoner();
    }


}
