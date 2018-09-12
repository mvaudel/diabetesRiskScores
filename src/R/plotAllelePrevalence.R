# 
# This script plots the allele prevalence in the categories of the different cohorts.
#

# Libraries

library(ggplot2)
library(scico)
library(ggrepel)


# Parameters

## Input files
harvestT1dOramAllelePrevalenceFile <- "resources/grs/harvest_T1dOram_allelePrevalence.gz"
harvestT2dDiagramAllelePrevalenceFile <- "resources/grs/harvest_T2dDiagram_allelePrevalence.gz"
harvestT2dDiamantAllelePrevalenceFile <- "resources/grs/harvest_T2dDiamant_allelePrevalence.gz"
rotterdamT1dOramAllelePrevalenceFile <- "resources/grs/rotterdam_T1dOram_allelePrevalence.gz"
rotterdamT2dDiagramAllelePrevalenceFile <- "resources/grs/rotterdam_T2dDiagram_allelePrevalence.gz"
rotterdamT2dDiamantAllelePrevalenceFile <- "resources/grs/rotterdam_T2dDiamant_allelePrevalence.gz"
modyT1dOramAllelePrevalenceFile <- "resources/grs/mody_T1dOram_allelePrevalence.gz"
modyT2dDiagramAllelePrevalenceFile <- "resources/grs/mody_T2dDiagram_allelePrevalence.gz"
modyT2dDiamantAllelePrevalenceFile <- "resources/grs/mody_T2dDiamant_allelePrevalence.gz"


## Doc files
plotFolder <- "docs/grs"


## Colors

palette <- 'roma'
t1dColor <- scico(n = 1, begin = 0.15, end = 0.15, palette = palette)
t2dColor <- scico(n = 1, begin = 0.85, end = 0.85, palette = palette)



# Main script

## Load data

print(paste(Sys.time(), " Loading data", sep = ""))

harvestT1dOramAllelePrevalenceDF <- read.table(harvestT1dOramAllelePrevalenceFile, sep = " ", header = T, stringsAsFactors = F)
harvestT2dDiagramAllelePrevalenceDF <- read.table(harvestT2dDiagramAllelePrevalenceFile, sep = " ", header = T, stringsAsFactors = F)
harvestT2dDiamantAllelePrevalenceDF <- read.table(harvestT2dDiamantAllelePrevalenceFile, sep = " ", header = T, stringsAsFactors = F)
rotterdamT1dOramAllelePrevalenceDF <- read.table(rotterdamT1dOramAllelePrevalenceFile, sep = " ", header = T, stringsAsFactors = F)
rotterdamT2dDiagramAllelePrevalenceDF <- read.table(rotterdamT2dDiagramAllelePrevalenceFile, sep = " ", header = T, stringsAsFactors = F)
rotterdamT2dDiamantAllelePrevalenceDF <- read.table(rotterdamT2dDiamantAllelePrevalenceFile, sep = " ", header = T, stringsAsFactors = F)
modyT1dOramAllelePrevalenceDF <- read.table(modyT1dOramAllelePrevalenceFile, sep = " ", header = T, stringsAsFactors = F)
modyT2dDiagramAllelePrevalenceDF <- read.table(modyT2dDiagramAllelePrevalenceFile, sep = " ", header = T, stringsAsFactors = F)
modyT2dDiamantAllelePrevalenceDF <- read.table(modyT2dDiamantAllelePrevalenceFile, sep = " ", header = T, stringsAsFactors = F)


## Formatting allele prevalence

t1dOramAllelePrevalenceDF <- rbind(harvestT1dOramAllelePrevalenceDF, rotterdamT1dOramAllelePrevalenceDF, modyT1dOramAllelePrevalenceDF)
t2dDiagramAllelePrevalenceDF <- rbind(harvestT2dDiagramAllelePrevalenceDF, rotterdamT2dDiagramAllelePrevalenceDF, modyT2dDiagramAllelePrevalenceDF)
t2dDiamantAllelePrevalenceDF <- rbind(harvestT2dDiamantAllelePrevalenceDF, rotterdamT2dDiamantAllelePrevalenceDF, modyT2dDiamantAllelePrevalenceDF)


t1dOramAllelePrevalenceDF$category <- factor(t1dOramAllelePrevalenceDF$category)
t1dOramAllelePrevalenceDF$feature <- factor(t1dOramAllelePrevalenceDF$feature)
t1dOramAllelePrevalenceDF$snp <- factor(t1dOramAllelePrevalenceDF$snp)
t1dOramAllelePrevalenceDF$type <- factor(t1dOramAllelePrevalenceDF$type)

t2dDiagramAllelePrevalenceDF$category <- factor(t2dDiagramAllelePrevalenceDF$category)
t2dDiagramAllelePrevalenceDF$feature <- factor(t2dDiagramAllelePrevalenceDF$feature)
t2dDiagramAllelePrevalenceDF$snp <- factor(t2dDiagramAllelePrevalenceDF$snp)
t2dDiagramAllelePrevalenceDF$type <- factor(t2dDiagramAllelePrevalenceDF$type)

t2dDiamantAllelePrevalenceDF$category <- factor(t2dDiamantAllelePrevalenceDF$category)
t2dDiamantAllelePrevalenceDF$feature <- factor(t2dDiamantAllelePrevalenceDF$feature)
t2dDiamantAllelePrevalenceDF$snp <- factor(t2dDiamantAllelePrevalenceDF$snp)
t2dDiamantAllelePrevalenceDF$type <- factor(t2dDiamantAllelePrevalenceDF$type)

categories <- c("MODY", "MODYX", "childhood", "diabetes", "gestational", "not_reported", "type_1", "type_2")

status <- c()
feature <- c()
weight <- c()
altFreq <- c()

i <- 1

for (featureI in levels(t1dOramAllelePrevalenceDF$feature)) {
    
    for (statusI in categories) {
        
        tempDF <- t1dOramAllelePrevalenceDF[t1dOramAllelePrevalenceDF$feature == featureI 
                                            & t1dOramAllelePrevalenceDF$category == statusI, ]
        
        if (nrow(tempDF) > 0) {
            
            typeI <- tempDF$type[1]
            
            if (typeI == "SingleAlleleFeature") {
                
                weightI <- tempDF$weight[1]
                
                status[i] <- statusI
                feature[i] <- featureI
                weight[i] <- weightI
                altFreq[i] <- (sum(tempDF$count[tempDF$genotype == 1]) + 2 * sum(tempDF$count[tempDF$genotype == 2])) / (2 * sum(tempDF$count))
                
            } else {
                
                weightI <- tempDF$weight[1]
                
                status[i] <- statusI
                feature[i] <- featureI
                weight[i] <- weightI
                altFreq[i] <- sum(tempDF$count[tempDF$genotype == 1]) / sum(tempDF$count)
                
            }
            
            i <- i +1
            
        }
    }
}

t1dOramAltFreqDF <- data.frame(status, feature, weight, altFreq)

status <- c()
feature <- c()
weight <- c()
altFreq <- c()

i <- 1

for (featureI in levels(t2dDiagramAllelePrevalenceDF$feature)) {
    
    for (statusI in categories) {
        
        tempDF <- t2dDiagramAllelePrevalenceDF[t2dDiagramAllelePrevalenceDF$feature == featureI 
                                            & t2dDiagramAllelePrevalenceDF$category == statusI, ]
        
        if (nrow(tempDF) > 0) {
            
            typeI <- tempDF$type[1]
            
            if (typeI == "SingleAlleleFeature") {
                
                weightI <- tempDF$weight[1]
                
                status[i] <- statusI
                feature[i] <- featureI
                weight[i] <- weightI
                altFreq[i] <- (sum(tempDF$count[tempDF$genotype == 1]) + 2 * sum(tempDF$count[tempDF$genotype == 2])) / (2 * sum(tempDF$count))
                
            } else {
                
                weightI <- tempDF$weight[1]
                
                status[i] <- statusI
                feature[i] <- featureI
                weight[i] <- weightI
                altFreq[i] <- sum(tempDF$count[tempDF$genotype == 1]) / sum(tempDF$count)
                
            }
            
            i <- i +1
            
        }
    }
}

t2dDiagramAltFreqDF <- data.frame(status, feature, weight, altFreq)

status <- c()
feature <- c()
weight <- c()
altFreq <- c()

i <- 1

for (featureI in levels(t2dDiamantAllelePrevalenceDF$feature)) {
    
    for (statusI in categories) {
        
        tempDF <- t2dDiamantAllelePrevalenceDF[t2dDiamantAllelePrevalenceDF$feature == featureI 
                                            & t2dDiamantAllelePrevalenceDF$category == statusI, ]
        
        if (nrow(tempDF) > 0) {
            
            typeI <- tempDF$type[1]
            
            if (typeI == "SingleAlleleFeature") {
                
                weightI <- tempDF$weight[1]
                
                status[i] <- statusI
                feature[i] <- featureI
                weight[i] <- weightI
                altFreq[i] <- (sum(tempDF$count[tempDF$genotype == 1]) + 2 * sum(tempDF$count[tempDF$genotype == 2])) / (2 * sum(tempDF$count))
                
            } else {
                
                weightI <- tempDF$weight[1]
                
                status[i] <- statusI
                feature[i] <- featureI
                weight[i] <- weightI
                altFreq[i] <- sum(tempDF$count[tempDF$genotype == 1]) / sum(tempDF$count)
                
            }
            
            i <- i +1
            
        }
    }
}

t2dDiamantAltFreqDF <- data.frame(status, feature, weight, altFreq)


# Plot allele prevalence in each category

t1dOramAltFreqDF$score <- "T1D"
t2dDiagramAltFreqDF$score <- "T2D"

altFreqDF <- rbind(t1dOramAltFreqDF, t2dDiagramAltFreqDF)
altFreqDF$score <- factor(altFreqDF$score)

xDF <- altFreqDF[altFreqDF$status == "not_reported", c("feature", "altFreq")]
names(xDF) <- c("feature", "x")
yDF <- altFreqDF[altFreqDF$status != "not_reported", ]
names(yDF) <- c("status", "feature", "weight", "y", "score")

plotDF <- merge(xDF, yDF, by = "feature", all = T)
plotDF$x[is.na(plotDF$x)] <- 0
plotDF$y[is.na(plotDF$y)] <- 0
plotDF$x <- 100 * plotDF$x
plotDF$y <- 100 * plotDF$y
plotDF <- plotDF[plotDF$weight > 0, ]
plotDF$status <- factor(plotDF$status, levels = c("diabetes", "childhood", "type_1", "type_2", "gestational", "MODY", "MODYX"))
levels(plotDF$status) <- c("Father Diabetes", "Childhood", "Mother Type 1", "Mother Type 2", "Gestational", "MODY", "MODYX")

plotDF$feature <- as.character(plotDF$feature)
is <- plotDF$x == 0 | plotDF$y == 0
plotDF$annotation <- ifelse(is & (plotDF$x != 0 | plotDF$y != 0), plotDF$feature, NA)
plotDF$relativeDeviation[!is] <- log10(plotDF$y[!is] / plotDF$x[!is])

category <- c()
x <- c()
yMin <- c()
yMax <- c()
score <- c()

for (status in levels(plotDF$status)) {
    
    is <- plotDF$x != 0 & plotDF$y != 0 & plotDF$status == status
    
    deviations <- plotDF$relativeDeviation[is]
    
    quantiles <- quantile(deviations, probs = c(0.05, 0.95), na.rm = T, names = F)
    
    is <- is & (plotDF$relativeDeviation < quantiles[1] | plotDF$relativeDeviation > quantiles[2])
    
    plotDF$annotation[is] <- plotDF$feature[is]
    
    xI <- 1:100
    yMinI <- (10^quantiles[1]) * xI
    yMaxI <- (10^quantiles[2]) * xI
    
    x <- c(x, xI)
    yMin <- c(yMin, yMinI)
    yMax <- c(yMax, yMaxI)
    category <- c(category, rep(status, 100))
    score <- c(score, rep("T1D", 100))
    
    x <- c(x, xI)
    yMin <- c(yMin, yMinI)
    yMax <- c(yMax, yMaxI)
    category <- c(category, rep(status, 100))
    score <- c(score, rep("T2D", 100))
    
}

backgroundDF <- data.frame(x, yMin, yMax, status = category, score = score)
backgroundDF$status <- factor(backgroundDF$status, levels = c("Father Diabetes", "Childhood", "Mother Type 1", "Mother Type 2", "Gestational", "MODY", "MODYX"))

plot <- ggplot() + theme_bw()

plot <- plot + geom_abline(intercept = 0, slope = 1, linetype = "dashed", color = "darkgray")

plot <- plot + geom_line(data = backgroundDF, aes(x = x, y = yMin), col = "darkgray", linetype = "dotted")
plot <- plot + geom_line(data = backgroundDF, aes(x = x, y = yMax), col = "darkgray", linetype = "dotted")

plot <- plot + geom_point(data = plotDF, aes(x = x, y = y, col = score, size = weight), alpha = 0.8)
plot <- plot + geom_text_repel(data = plotDF[!is.na(plotDF$annotation), ], aes(x = x, y = y, label = annotation))

plot <- plot + scale_color_manual(values = c(t1dColor, t2dColor))
plot <- plot + scale_size_continuous(range = c(0.5, 4))

plot <- plot + scale_x_continuous(name = "Controls - Effect Allee Frequency [%]", breaks = 20 * (0:5), limits = c(0, 100))
plot <- plot + scale_y_continuous(name = "Cases - Effect Allee Frequency [%]", breaks = 20 * (0:5), limits = c(0, 100))

plot <- plot + theme(legend.position = "none",
                     strip.background =element_rect(fill="grey95"))

plot <- plot + facet_grid(status ~ score)

png("docs/grs/altFreq.png", height = 48, width = 16, units = "cm", res = 300)
plot(plot)
dummy <- dev.off()

pdf("docs/grs/altFreq.pdf", height = unit(24, "cm"), width = unit(8, "cm"))
plot(plot)
dummy <- dev.off()


# Plot allele prevalence in MODYX against other conditions

yDF <- altFreqDF[altFreqDF$status == "MODYX", c("feature", "altFreq")]
names(yDF) <- c("feature", "y")

xDF <- altFreqDF[altFreqDF$status != "MODYX", ]
names(xDF) <- c("status", "feature", "weight", "x", "score")

plotDF <- merge(xDF, yDF, by = "feature", all = T)
plotDF$x[is.na(plotDF$x)] <- 0
plotDF$y[is.na(plotDF$y)] <- 0
plotDF$x <- 100 * plotDF$x
plotDF$y <- 100 * plotDF$y
plotDF <- plotDF[plotDF$weight > 0, ]
plotDF$status <- factor(plotDF$status, levels = c("not_reported", "diabetes", "childhood", "type_1", "type_2", "gestational", "MODY"))
levels(plotDF$status) <- c("Not Reported", "Father Diabetes", "Childhood", "Mother Type 1", "Mother Type 2", "Gestational", "MODY")

plotDF$feature <- as.character(plotDF$feature)
is <- plotDF$x == 0 | plotDF$y == 0
plotDF$annotation <- ifelse(is & (plotDF$x != 0 | plotDF$y != 0), plotDF$feature, NA)
plotDF$relativeDeviation[!is] <- log10(plotDF$y[!is] / plotDF$x[!is])

category <- c()
x <- c()
yMin <- c()
yMax <- c()
score <- c()

for (status in levels(plotDF$status)) {
    
    is <- plotDF$x != 0 & plotDF$y != 0 & plotDF$status == status
    
    deviations <- plotDF$relativeDeviation[is]
    
    quantiles <- quantile(deviations, probs = c(0.05, 0.95), na.rm = T, names = F)
    
    is <- is & (plotDF$relativeDeviation < quantiles[1] | plotDF$relativeDeviation > quantiles[2])
    
    plotDF$annotation[is] <- plotDF$feature[is]
    
    xI <- 1:100
    yMinI <- (10^quantiles[1]) * xI
    yMaxI <- (10^quantiles[2]) * xI
    
    x <- c(x, xI)
    yMin <- c(yMin, yMinI)
    yMax <- c(yMax, yMaxI)
    category <- c(category, rep(status, 100))
    score <- c(score, rep("T1D", 100))
    
    x <- c(x, xI)
    yMin <- c(yMin, yMinI)
    yMax <- c(yMax, yMaxI)
    category <- c(category, rep(status, 100))
    score <- c(score, rep("T2D", 100))
    
}

backgroundDF <- data.frame(x, yMin, yMax, status = category, score = score)
backgroundDF$status <- factor(backgroundDF$status, levels = c("Not Reported", "Father Diabetes", "Childhood", "Mother Type 1", "Mother Type 2", "Gestational", "MODY"))

plot <- ggplot() + theme_bw()

plot <- plot + geom_abline(intercept = 0, slope = 1, linetype = "dashed", color = "darkgray")

plot <- plot + geom_line(data = backgroundDF, aes(x = x, y = yMin), col = "darkgray", linetype = "dotted")
plot <- plot + geom_line(data = backgroundDF, aes(x = x, y = yMax), col = "darkgray", linetype = "dotted")

plot <- plot + geom_point(data = plotDF, aes(x = x, y = y, col = score, size = weight), alpha = 0.8)
plot <- plot + geom_text_repel(data = plotDF[!is.na(plotDF$annotation), ], aes(x = x, y = y, label = annotation))

plot <- plot + scale_color_manual(values = c(t1dColor, t2dColor))
plot <- plot + scale_size_continuous(range = c(0.5, 4))

plot <- plot + scale_x_continuous(name = "Controls - Effect Allee Frequency [%]", breaks = 20 * (0:5), limits = c(0, 100))
plot <- plot + scale_y_continuous(name = "Cases - Effect Allee Frequency [%]", breaks = 20 * (0:5), limits = c(0, 100))

plot <- plot + theme(legend.position = "none",
                     strip.background =element_rect(fill="grey95"))

plot <- plot + facet_grid(status ~ score)

png("docs/grs/altFreq_MODYX.png", height = 48, width = 16, units = "cm", res = 300)
plot(plot)
dummy <- dev.off()

pdf("docs/grs/altFreq_MODYX.pdf", height = unit(24, "cm"), width = unit(8, "cm"))
plot(plot)
dummy <- dev.off()

