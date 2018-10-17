package no.uib.drs.cmd;

import htsjdk.variant.vcf.VCFFileReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import no.uib.drs.io.Utils;
import no.uib.drs.io.json.SimpleObjectMapper;
import no.uib.drs.io.vcf.GenotypeProvider;
import no.uib.drs.io.vcf.VariantCoordinatesMap;
import no.uib.drs.io.vcf.VariantDetailsProvider;
import no.uib.drs.model.biology.Proxy;
import no.uib.drs.model.score.RiskScore;
import no.uib.drs.model.biology.Variant;
import no.uib.drs.model.score.VariantFeatureMap;
import no.uib.drs.utils.ProgressHandler;

/**
 * This class writes the summary for a given score on a set of vcf files.
 *
 * @author Marc Vaudel
 */
public class ComputeGrs {

    /**
     * Writes a summary.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public ComputeGrs() {

    }

    private void writeScores(File scoreDetailsFile, File proxiesMapFile, ArrayList<File> vcfFiles, ArrayList<File> variantDetailsFiles, File destinationFile) {

        ProgressHandler progressHandler = new ProgressHandler();

        String mainTaskName = "1. Computing GRS";
        progressHandler.start(mainTaskName);

        String taskName = "1.1 Loading score details";
        progressHandler.start(taskName);

        RiskScore riskScore = SimpleObjectMapper.read(scoreDetailsFile, RiskScore.class);
        VariantFeatureMap variantFeatureMap = new VariantFeatureMap(riskScore);

        progressHandler.end(taskName);

        taskName = "1.2 Loading samples details";
        progressHandler.start(taskName);

        VCFFileReader vcfFileReader = new VCFFileReader(vcfFiles.get(0));
        ArrayList<String> sampleNames = vcfFileReader.getFileHeader().getSampleNamesInOrder();
        vcfFileReader.close();

        progressHandler.end(taskName);

        String taskName = "1.1 Loading proxies";
        progressHandler.start(taskName);

        HashMap<String, String> proxyIds = Proxy.getProxyMap(proxiesMapFile);

        progressHandler.end(taskName);

        taskName = "1.1 Loading variant details";
        progressHandler.start(taskName);;

        int nVcf = vcfFiles.size();
        HashMap<String, VariantDetailsProvider> variantCoordinatesMap = new HashMap<>(nVcf);
        IntStream.range(0, nVcf)
                .parallel()
                .forEach(i -> variantCoordinatesMap.put(vcfFiles.get(i).getName(),
                VariantCoordinatesMap.getVariantCoordinatesMap(variantDetailsFiles.get(i), variantFeatureMap, proxyIds)));

        GenotypeProvider genotypeProvider = new GenotypeProvider();
        vcfFilesPath.forEach(filePath -> genotypeProvider.addVcfFile(new File(filePath), Utils.getVcfIndexFile(filePath));
        
        

        progressHandler.end(taskName);

        taskName = "1.2 Loading GRS";
        progressHandler.start(taskName);

        GRS t1dOram = ScorePool.getT1dOram();
        GRS t2dDiamant = ScorePool.getT2dDiamant();
        GRS t2dDiagram = ScorePool.getT2dDiagram();

        GRS[] scores = {t1dOram, t2dDiamant, t2dDiagram};

        progressHandler.end(taskName);

        taskName = "1.3 Loading proxies";
        progressHandler.start(taskName);

        HashMap<String, Proxy> allProxiesMap = new HashMap<>(0);
        allProxiesMap.putAll(ScorePool.getProxyMap(t1dOram));
        allProxiesMap.putAll(ScorePool.getProxyMap(t2dDiamant));
        allProxiesMap.putAll(ScorePool.getProxyMap(t2dDiagram));

        HashMap<String, Proxy> proxiesMap = new HashMap<>(allProxiesMap.size());

        for (Entry<String, Proxy> entry : allProxiesMap.entrySet()) {

            String snp = entry.getKey();
            Proxy proxy = entry.getValue();

            boolean variantOK = true;

            for (Cohort cohort : cohorts) {

                VariantCoordinatesMap variantDetailsProvider = variantDetailsProviders.get(cohort.name());
                Variant variant = variantDetailsProvider.getVariant(proxy.proxyId);

                if (variant == null || !variant.genotyped && variant.info < 0.7) {

                    variantOK = false;
                    break;

                }
            }

            if (variantOK) {

                proxiesMap.put(snp, proxy);

            }
        }

        progressHandler.end(taskName);

        taskName = "1.4 Setting up vcf file readers";
        progressHandler.start(taskName);

        HashMap<String, GenotypeProvider> genotypeProviders = Arrays.stream(cohorts)
                .parallel()
                .collect(Collectors.toMap(
                        cohort -> cohort.name(),
                        cohort -> new GenotypeProvider(),
                        (a, b) -> {
                            throw new IllegalArgumentException("Duplicate cohort name");
                        },
                        HashMap::new));

        Arrays.stream(cohorts)
                .forEach(cohort -> Arrays.stream(chromosomes)
                .parallel()
                .forEach(chromosome -> genotypeProviders.get(cohort.name()).addVcfFile(chromosome, cohort.getVcfFile(chromosome), cohort.getVcfIndexFile(chromosome))));

        progressHandler.end(taskName);

        taskName = "1.2 Loading moba pheno details";
        progressHandler.start(taskName);

        ModyInfoMap modyInfoMap = ModyInfoMap.getModyInfoMap(modyInfoFile, modyEthnicityFile, modyRelatednessFile);

        progressHandler.end(taskName);

        taskName = "1.3 Loading moba pheno details";
        progressHandler.start(taskName);

        HashSet<String> samples = new HashSet<>();

        GenotypeProvider harvestGenotypeProvider = genotypeProviders.get(Cohort.harvest.name());
        samples.addAll(harvestGenotypeProvider.getSamples());

        GenotypeProvider rotterdamGenotypeProvider = genotypeProviders.get(Cohort.rotterdam.name());
        samples.addAll(rotterdamGenotypeProvider.getSamples());

        MobaPhenoMap mobaPhenoMap = MobaPhenoMap.getMobaPhenoMap(mobaPhenoFolder, samples);

        progressHandler.end(taskName);

        HashMap<String, HashMap<String, Double>> backgroundScores = new HashMap<>(0);

        HashMap<String, ScoreGenerator> scoreGeneratoreMap = new HashMap<>(cohorts.length);

        int progress = 5;

        for (Cohort cohort : cohorts) {

            GenotypeProvider genotypeProvider = genotypeProviders.get(cohort.name());
            VariantCoordinatesMap variantDetailsProvider = variantDetailsProviders.get(cohort.name());

            taskName = "1." + progress++ + " Estimating scores in " + cohort.name();
            progressHandler.start(taskName);

            HashMap<String, String> categoriesMap = cohort == Cohort.mody
                    ? modyInfoMap.statusMap
                    : mobaPhenoMap.diabetesStatusMap.entrySet().stream()
                            .collect(Collectors.toMap(
                                    entry -> entry.getKey(),
                                    entry -> entry.getValue().name(),
                                    (a, b) -> a,
                                    HashMap::new));

            ScoreGenerator scoreGenerator = new ScoreGenerator(genotypeProvider, variantDetailsProvider, proxiesMap, categoriesMap, scores);
            scoreGeneratoreMap.put(cohort.name(), scoreGenerator);

            progressHandler.end(taskName);

            taskName = "1." + progress++ + " Exporting scores in " + cohort.name();
            progressHandler.start(taskName);

            HashMap<String, HashMap<String, Double>> scoreMap = scoreGenerator.getScoreMap();
            TreeMap<String, HashMap<String, Double>> sortedScoreMap = new TreeMap<>(scoreMap);

            File scoresFile = new File(outputFolder, cohort.name() + "_scores.gz");
            File anonymizedScoresFile = new File(anonymizedOutputFolder, cohort.name() + "_scores.gz");

            int index = 1;
            int familyIndex = 1;
            HashMap<String, Integer> familyIndexMap = new HashMap<>();

            HashSet<String> mobaSamples = cohort == Cohort.mody ? null : new HashSet<>(mobaPhenoMap.roleMap.keySet());

            try (final SimpleGzWriter writer = new SimpleGzWriter(scoresFile)) {

                try (final SimpleGzWriter anonymizedWriter = new SimpleGzWriter(anonymizedScoresFile)) {

                    String scoreColumns = Arrays.stream(scores).map(score -> score.name).collect(Collectors.joining(separator));
                    writer.writeLine("sample", "role", "sex", "age", "family", "category", "knownVariant", scoreColumns);
                    anonymizedWriter.writeLine("sample", "role", "sex", "age", "family", "category", "knownVariant", scoreColumns);

                    for (Entry<String, HashMap<String, Double>> entry : sortedScoreMap.entrySet()) {

                        String sample = entry.getKey();

                        String sampleId, role, sex, age, family, familyId, category, knownVariant;
                        if (cohort == Cohort.mody) {

                            sex = modyInfoMap.sexMap.get(sample);
                            family = modyInfoMap.relatednessMap.get(sample);
                            category = modyInfoMap.statusMap.get(sample);
                            knownVariant = modyInfoMap.knownVariantMap.get(sample);
                            role = "NA";

                            Integer ageTemp = modyInfoMap.ageMap.get(sample);
                            age = ageTemp == null ? "NA" : ageTemp.toString();

                            Integer tempIndex = familyIndexMap.get(family);
                            if (tempIndex == null) {
                                tempIndex = familyIndex++;
                                familyIndexMap.put(family, tempIndex);
                            }
                            familyId = tempIndex.toString();
                            sampleId = Integer.toString(index++);

                        } else {

                            if (!mobaSamples.contains(sample)) {
                                continue;
                            }

                            sex = mobaPhenoMap.sexMap.get(sample);
                            family = "NA";
                            if (mobaPhenoMap.diabetesStatusMap.get(sample) == null) {
                                throw new IllegalArgumentException("Missing diabetes status for sample " + sample);
                            }
                            knownVariant = "NA";
                            category = mobaPhenoMap.diabetesStatusMap.get(sample).name();

                            Integer ageTemp = mobaPhenoMap.ageMap.get(sample);
                            age = ageTemp == null ? "NA" : ageTemp.toString();

                            familyId = "NA";
                            sampleId = Integer.toString(index++);

                            role = mobaPhenoMap.roleMap.get(sample);

                        }

                        HashMap<String, Double> scoreValues = entry.getValue();
                        String scoresString = Arrays.stream(scores)
                                .map(score -> scoreValues.get(score.name).toString())
                                .collect(Collectors.joining(separator));

                        writer.writeLine(sample, role, sex, age, family, category, knownVariant, scoresString);
                        anonymizedWriter.writeLine(sampleId, role, sex, age, familyId, category, knownVariant, scoresString);

                    }
                }
            }

            progressHandler.end(taskName);

            taskName = "1." + progress++ + " Exporting allele prevalence in " + cohort.name();
            progressHandler.start(taskName);

            TreeMap<String, HashMap<String, HashMap<String, int[]>>> allelePrevalenceMap = new TreeMap(scoreGenerator.getAllelePrevalenceMap());

            for (GRS grs : scores) {

                File outputFile = new File(anonymizedOutputFolder, cohort.name() + "_" + grs.name + "_allelePrevalence.gz");

                try (final SimpleGzWriter bw = new SimpleGzWriter(outputFile)) {

                    bw.writeLine("cohort", "category", "feature", "type", "weight", "snp", "proxy", "genotype", "count");

                    for (Entry<String, HashMap<String, HashMap<String, int[]>>> entry : allelePrevalenceMap.entrySet()) {

                        String category = entry.getKey();
                        HashMap<String, HashMap<String, int[]>> categoryMap = entry.getValue();

                        for (ScoringFeature scoringFeature : grs.features) {

                            String name = scoringFeature.getName();

                            String type = scoringFeature.getType();

                            String snpNames = String.join(",", scoringFeature.getRsIds());

                            String weight = Double.toString(scoringFeature.getWeight());

                            String proxy = Arrays.stream(scoringFeature.getRsIds())
                                    .map(snp -> proxiesMap.containsKey(snp) ? proxiesMap.get(snp).proxyId : "NA")
                                    .collect(Collectors.joining(","));

                            int[] allelePrevalences = categoryMap.get(type).get(name);

                            if (allelePrevalences == null) {

                                bw.writeLine(cohort.name(), category, name, type, weight, snpNames, proxy, "0", "NA");
                                bw.writeLine(cohort.name(), category, name, type, weight, snpNames, proxy, "1", "NA");

                                if (type.equals(SingleAlleleFeature.type)) {
                                    bw.writeLine(cohort.name(), category, name, type, weight, snpNames, proxy, "2", "NA");
                                }

                            } else {

                                for (int i = 0; i < allelePrevalences.length; i++) {

                                    int allelePrevalence = allelePrevalences[i];

                                    bw.writeLine(cohort.name(), category, name, type, weight, snpNames, proxy, Integer.toString(i), Integer.toString(allelePrevalence));

                                }
                            }
                        }
                    }
                }
            }

            progressHandler.end(taskName);

            if (cohort != Cohort.mody) {

                taskName = "1." + progress++ + " Gathering background scores in " + cohort.name();
                progressHandler.start(taskName);

                scoreMap.entrySet().stream()
                        .filter(entry -> mobaPhenoMap.diabetesStatusMap.containsKey(entry.getKey())
                        && mobaPhenoMap.diabetesStatusMap.get(entry.getKey()) == not_reported
                        && mobaPhenoMap.roleMap.containsKey(entry.getKey())
                        && !mobaPhenoMap.roleMap.get(entry.getKey()).equals("Kid"))
                        .forEach(entry -> backgroundScores.put(entry.getKey(), entry.getValue()));

                progressHandler.end(taskName);

            }
        }

        taskName = "1." + progress++ + " Extracting decile scoring profiles";
        progressHandler.start(taskName);

        HashMap<String, HashMap<String, HashMap<Integer, HashMap<String, HashMap<String, Double>>>>> contributionsMap = new HashMap<>(scores.length);
        HashMap<String, HashMap<String, HashMap<Integer, HashMap<String, HashSet<String>>>>> nSamplesMap = new HashMap<>(scores.length);

        for (Cohort cohort : cohorts) {

            GenotypeProvider genotypeProvider = genotypeProviders.get(cohort.name());

            ScoreGenerator scoreGenerator = scoreGeneratoreMap.get(cohort.name());
            HashMap<String, HashMap<String, Double>> scoreMap = scoreGenerator.getScoreMap();

            HashMap<String, String> categoriesMap = cohort == Cohort.mody
                    ? modyInfoMap.statusMap
                    : mobaPhenoMap.diabetesStatusMap.entrySet().stream()
                            .collect(Collectors.toMap(
                                    entry -> entry.getKey(),
                                    entry -> entry.getValue().name(),
                                    (a, b) -> a,
                                    HashMap::new));

            for (String sample : genotypeProvider.getSamples()) {

                String category = categoriesMap.get(sample);

                HashMap<String, HashMap<Integer, HashMap<String, HashMap<String, Double>>>> categoriesContributions = contributionsMap.get(category);

                if (categoriesContributions == null) {

                    categoriesContributions = new HashMap<>(scores.length);
                    contributionsMap.put(category, categoriesContributions);

                }

                HashMap<String, HashMap<Integer, HashMap<String, HashSet<String>>>> nCategoriesContributions = nSamplesMap.get(category);

                if (nCategoriesContributions == null) {

                    nCategoriesContributions = new HashMap<>(scores.length);
                    nSamplesMap.put(category, nCategoriesContributions);

                }

                for (GRS score1 : scores) {

                    ArrayList<Double> sortedScores = backgroundScores.values().stream()
                            .flatMap(map -> map.entrySet().stream())
                            .filter(entry -> entry.getKey().equals(score1.name))
                            .map(entry -> entry.getValue())
                            .sorted()
                            .collect(Collectors.toCollection(ArrayList::new));

                    HashMap<Integer, HashMap<String, HashMap<String, Double>>> scoreContributions = categoriesContributions.get(score1.name);

                    if (scoreContributions == null) {

                        scoreContributions = new HashMap<>(10);
                        categoriesContributions.put(score1.name, scoreContributions);

                    }

                    HashMap<Integer, HashMap<String, HashSet<String>>> nScoreContributions = nCategoriesContributions.get(score1.name);

                    if (nScoreContributions == null) {

                        nScoreContributions = new HashMap<>(10);
                        nCategoriesContributions.put(score1.name, nScoreContributions);

                    }

                    for (int i = 0; i < 10; i++) {

                        HashMap<String, HashMap<String, Double>> binMap = scoreContributions.get(i);

                        if (binMap == null) {

                            binMap = new HashMap<>(scores.length);
                            scoreContributions.put(i, binMap);

                        }

                        HashMap<String, HashSet<String>> nBinMap = nScoreContributions.get(i);

                        if (nBinMap == null) {

                            nBinMap = new HashMap<>(scores.length);
                            nScoreContributions.put(i, nBinMap);

                        }

                        for (GRS score2 : scores) {

                            HashMap<String, Double> scoreMap2 = binMap.get(score2.name);

                            if (scoreMap2 == null) {

                                scoreMap2 = new HashMap<>(score2.features.size());
                                binMap.put(score2.name, scoreMap2);

                                for (ScoringFeature feature : score2.features) {

                                    scoreMap2.put(feature.getName(), 0.0);

                                }
                            }

                            HashSet<String> binSamples = nBinMap.get(score2.name);

                            if (binSamples == null) {

                                binSamples = new HashSet<>(1);
                                nBinMap.put(score2.name, binSamples);

                            }

                            double scoreMin = Util.percentileSorted(sortedScores, ((double) i) / 10.0);
                            double scoreMax = Util.percentileSorted(sortedScores, ((double) (i + 1)) / 10.0);

                            double value1 = scoreMap.get(sample).get(score1.name);

                            if (value1 > scoreMin && value1 <= scoreMax) {

                                binSamples.add(sample);

                                for (ScoringFeature feature : score2.features) {

                                    String featureName = feature.getName();
                                    double featureContribution = scoreGenerator.getFeatureScore(feature, sample);
                                    scoreMap2.put(featureName, scoreMap2.get(featureName) + featureContribution);

                                }
                            }
                        }
                    }
                }
            }
        }

        File outputFile = new File(anonymizedOutputFolder, "scoreContribution.gz");

        try (final SimpleGzWriter bw = new SimpleGzWriter(outputFile)) {

            bw.writeLine("category", "score1", "bin", "score2", "feature", "weight");

            for (Entry<String, HashMap<String, HashMap<Integer, HashMap<String, HashMap<String, Double>>>>> entry1 : contributionsMap.entrySet()) {

                String category = entry1.getKey();
                HashMap<String, HashMap<Integer, HashMap<String, HashMap<String, Double>>>> map1 = entry1.getValue();

                for (Entry<String, HashMap<Integer, HashMap<String, HashMap<String, Double>>>> entry2 : map1.entrySet()) {

                    String score1 = entry2.getKey();
                    HashMap<Integer, HashMap<String, HashMap<String, Double>>> map2 = entry2.getValue();

                    for (Entry<Integer, HashMap<String, HashMap<String, Double>>> entry3 : map2.entrySet()) {

                        String bin = entry3.getKey().toString();
                        HashMap<String, HashMap<String, Double>> map3 = entry3.getValue();

                        for (Entry<String, HashMap<String, Double>> entry4 : map3.entrySet()) {

                            String score2 = entry4.getKey();
                            HashMap<String, Double> map4 = entry4.getValue();

                            for (Entry<String, Double> entry5 : map4.entrySet()) {

                                String feature = entry5.getKey();
                                String weight = entry5.getValue().toString();

                                bw.writeLine(category, score1, bin, score2, feature, weight);

                            }
                        }
                    }
                }
            }
        }

        outputFile = new File(anonymizedOutputFolder, "scoreContributionN.gz");

        try (final SimpleGzWriter bw = new SimpleGzWriter(outputFile)) {

            bw.writeLine("category", "score1", "bin", "score2", "nSamples");

            for (Entry<String, HashMap<String, HashMap<Integer, HashMap<String, HashSet<String>>>>> entry1 : nSamplesMap.entrySet()) {

                String category = entry1.getKey();
                HashMap<String, HashMap<Integer, HashMap<String, HashSet<String>>>> map1 = entry1.getValue();

                for (Entry<String, HashMap<Integer, HashMap<String, HashSet<String>>>> entry2 : map1.entrySet()) {

                    String score1 = entry2.getKey();
                    HashMap<Integer, HashMap<String, HashSet<String>>> map2 = entry2.getValue();

                    for (Entry<Integer, HashMap<String, HashSet<String>>> entry3 : map2.entrySet()) {

                        String bin = entry3.getKey().toString();
                        HashMap<String, HashSet<String>> map3 = entry3.getValue();

                        for (Entry<String, HashSet<String>> entry4 : map3.entrySet()) {

                            String score2 = entry4.getKey();
                            HashSet<String> sampleSet = entry4.getValue();
                            String tempNSamples = Integer.toString(sampleSet.size());

                            bw.writeLine(category, score1, bin, score2, tempNSamples);

                        }
                    }
                }
            }
        }

        progressHandler.end(taskName);

        taskName = "1." + progress++ + " Closing connection to files";
        progressHandler.start(taskName);

        genotypeProviders.values()
                .forEach(genotypeProvider -> genotypeProvider.close());

        progressHandler.end(taskName);

        progressHandler.end(mainTaskName);

    }
}
