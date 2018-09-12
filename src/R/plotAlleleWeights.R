# 
# This script plots the allele weights in the different scores for the patient categories.
#

# Libraries

library(ggplot2)
library(scico)
library(ggrepel)


# Parameters

## Input files
scoreContributionFile <- "resources/grs/scoreContribution.gz"
nSamplesFile <- "resources/grs/scoreContributionN.gz"

t1dMarkersFile <- "resources/markers/Oram_T1D-GRS.txt"
t2dMarkersFile <- "resources/markers/Diagram_T2D-loci.txt"


## Doc files
plotFolder <- "docs/grs"


# Functions



# Main script

## Load data

print(paste(Sys.time(), " Loading data", sep = ""))

scoreContributionDF <- read.table(scoreContributionFile, sep = " ", header = T, stringsAsFactors = F)
nSamplesDF <- read.table(nSamplesFile, sep = " ", header = T, stringsAsFactors = F)

t1dMarkers <- read.table(t1dMarkersFile, sep = "\t", header = T, stringsAsFactors = F)
t1dMarkers$id <- ifelse(regexpr(t1dMarkers$SNP, pattern = ",") > 0, t1dMarkers$Gene, t1dMarkers$SNP)
t1dMarkers$weight <- t1dMarkers$Weight
t1dMarkers <- t1dMarkers[order(t1dMarkers$weight, decreasing = T), c("id", "weight")]

t2dMarkers <- read.table(t2dMarkersFile, sep = "\t", header = T, stringsAsFactors = F)
t2dMarkers$id <- t2dMarkers$rsid
t2dMarkers$weight <- substr(t2dMarkers$OR..95..CI., start = 1, stop = regexpr(t2dMarkers$OR..95..CI., pattern = " ")-1)
t2dMarkers <- t2dMarkers[order(t2dMarkers$weight, decreasing = F), c("id", "weight")]


## Plot top percentile in each category

t1dFeatures <- t1dMarkers$id
t2dFeatures <- t2dMarkers$id
commonFeatures <- t1dFeatures[t1dFeatures %in% t2dFeatures]
t1dFeatures <- t1dFeatures[!t1dFeatures %in% commonFeatures]
t2dFeatures <- t2dFeatures[!t2dFeatures %in% commonFeatures]

t1dFeatureColor <- scico(n = length(t1dFeatures), begin = 0, end = 0.45, direction = 1, palette = "roma")
commonColors <- scico(n = length(commonFeatures), begin = 0.75, end = 1, direction = 1, palette = "grayC")
t2dFeatureColor <- scico(n = length(t2dFeatures), begin = 0.55, end = 1, direction = -1, palette = "roma")

features <- c(t1dFeatures, t2dFeatures, commonFeatures)
featuresColors <- c(t1dFeatureColor, t2dFeatureColor, commonColors)


plotDF <- scoreContributionDF[!scoreContributionDF$category %in% c("null", "other_gestational") 
                              & scoreContributionDF$score1 != "T2dDiamant"
                              & scoreContributionDF$score2 != "T2dDiamant"
                              & scoreContributionDF$score1 == scoreContributionDF$score2, ]

for (category in unique(plotDF$category)) {
    for (score in unique(plotDF$score1)) {
        
        n <- sum(nSamplesDF$nSamples[nSamplesDF$score1 == score & nSamplesDF$score2 == score & nSamplesDF$category == category])
        
        if (n == 0) {
            stop("No samples in category.")
        }
        
        plotIs <- plotDF$score1 == score & plotDF$category == category
        plotDF$y[plotIs] <- ifelse(plotDF$score1[plotIs] == "T1dOram", -plotDF$weight[plotIs]/n, plotDF$weight[plotIs]/n)
        
    }
}

plotDF <- plotDF[plotDF$y != 0, ]

plotDF$category <- factor(plotDF$category, levels = c("not_reported", "diabetes", "childhood", "type_1", "type_2", "gestational", "MODY", "MODYX"))
levels(plotDF$category) <- c("Not Reported", "Father Diabetes", "Childhood", "Mother Type 1", "Mother Type 2", "Gestational", "MODY", "MODYX")

plotDF$score1 <- factor(plotDF$score1, levels = c("T1dOram", "T2dDiagram"))
levels(plotDF$score1) <- c("T1D", "T2D")

plotDF$feature <- factor(plotDF$feature, levels = features)

plot <- ggplot() + theme_bw()

plot <- plot + geom_col(data = plotDF, aes(x = bin, y = y, fill = feature))

plot <- plot + scale_fill_manual(values = featuresColors)

plot <- plot + coord_flip()

plot <- plot + facet_grid(category ~ .)

plot <- plot + theme(legend.position = "bottom",
                     legend.title = element_blank())


png("docs/grs/weight.png", height = 48, width = 16, units = "cm", res = 300)
plot(plot)
dummy <- dev.off()

pdf("docs/grs/weight.pdf", height = unit(24, "cm"), width = unit(8, "cm"))
plot(plot)
dummy <- dev.off()

