# 
# This script plots summary information on the proxies.
#

# Libraries

library(ggplot2)
library(scico)


# Parameters

## Input files
t1dOramProxiesFile <- "resources/markers/Oram_T1D-proxies.gz"
t2dDiagramProxiesFile <- "resources/markers/Diagram_T2D-proxies.gz"
t2dDiamantProxiesFile <- "resources/markers/Diamant_T2D-proxies.gz"

## Doc files
plotFolder <- "docs/proxies"


# Functions



# Main script

## Load data

print(paste(Sys.time(), " Loading data", sep = ""))

t1dOramProxies <- read.table(t1dOramProxiesFile, header = T, stringsAsFactors = F)
t2dDiagramProxies <- read.table(t2dDiagramProxiesFile, header = T, stringsAsFactors = F)
t2dDiamantProxies <- read.table(t2dDiamantProxiesFile, header = T, stringsAsFactors = F)


## Formatting

print(paste(Sys.time(), " Formatting", sep = ""))

t1dOramProxies$score <- "T1D_Oram"
t2dDiagramProxies$score <- "T2D_Diagram"
t2dDiamantProxies$score <- "T2D_Diamant"

proxies <- rbind(t1dOramProxies, t2dDiagramProxies, t2dDiamantProxies)

proxies$cohort <- factor(proxies$cohort)
proxies$score <- factor(proxies$score)

cohortLevels <- levels(proxies$cohort)
scoreLevels <- levels(proxies$score)


## Number of proxies

print(paste(Sys.time(), " Exporting number of proxies", sep = ""))

nSnps <- c()
weightLosses <- c()
category <- c()
scores <- c()
nMax <- 0

for (score in scoreLevels) {
    
    proxiesTemp <- proxies[proxies$cohort == "harvest" & proxies$score == score, ]
    
    sameTemp <- sum(proxiesTemp$snp == proxiesTemp$proxy)
    sameWeight <- sum(proxiesTemp$weightLoss[proxiesTemp$snp == proxiesTemp$proxy])
    
    differentTemp <- sum(proxiesTemp$snp != proxiesTemp$proxy)
    differentWeight <- sum(proxiesTemp$weightLoss[proxiesTemp$snp != proxiesTemp$proxy])
    
    weightTotal <- sum(proxiesTemp$weightLoss)
    sameWeight <- 100.0 * sameWeight / weightTotal
    differentWeight <- 100.0 * differentWeight / weightTotal
    
    nSnps <- c(nSnps, sameTemp)
    weightLosses <- c(weightLosses, sameWeight)
    category <- c(category, "Original")
    scores <- c(scores, score)
    
    nSnps <- c(nSnps, differentTemp)
    weightLosses <- c(weightLosses, differentWeight)
    category <- c(category, "Proxy")
    scores <- c(scores, score)
    
    nMax <- max(nMax, nrow(proxiesTemp))
    
}

plotDF <- data.frame(score = scores, weightLoss = weightLosses, category, n = nSnps, stringsAsFactors = F)
plotDF$score <- factor(plotDF$score)
plotDF$category <- factor(plotDF$category, levels = c("Proxy", "Original"))

plot <- ggplot() + theme_bw()

plot <- plot + geom_col(data = plotDF, aes(x = score, y = n, col = category, fill = category))

plot <- plot + scale_color_manual(values = c("blue3", "black"))
plot <- plot + scale_fill_manual(values = c("blue3", "black"))

plot <- plot + scale_y_continuous(name = "# markers", expand = c(0, 0), limits = c(0, 1.05 * nMax))

plot <- plot + theme(legend.position = "top",
                     legend.title = element_blank(),
                     axis.title.x = element_blank())


png(file.path(plotFolder, "nProxies.png"), height = 8, width = 12, units = "cm", res = 300)
plot(plot)
dummy <- dev.off()

pdf(file.path(plotFolder, "nProxies.pdf"))
plot(plot)
dummy <- dev.off()

plot <- ggplot() + theme_bw()

plot <- plot + geom_col(data = plotDF, aes(x = score, y = weightLoss, col = category, fill = category))

plot <- plot + scale_color_manual(values = c("blue3", "black"))
plot <- plot + scale_fill_manual(values = c("blue3", "black"))

plot <- plot + scale_y_continuous(name = "Max weight loss [%]", expand = c(0, 0), limits = c(0, 105))

plot <- plot + theme(legend.position = "top",
                     legend.title = element_blank(),
                     axis.title.x = element_blank())


png(file.path(plotFolder, "nProxies_weight.png"), height = 8, width = 12, units = "cm", res = 300)
plot(plot)
dummy <- dev.off()

pdf(file.path(plotFolder, "nProxies_weight.pdf"))
plot(plot)
dummy <- dev.off()


## Genotyping for each score

print(paste(Sys.time(), " Exporting number of genotyped values", sep = ""))

genotypingColors <- scico(n = 4, begin = 0, end = 1, palette = "lajolla")

for (score in scoreLevels) {
    
    nSnps <- c()
    weightLosses <- c()
    genotyped <- c()
    cohorts <- c()
    category <- c()
    nMax <- 0
    
    for (cohort in cohortLevels) {
        
        proxiesTemp <- proxies[proxies$cohort == cohort & proxies$score == score, ]
        
        missingTemp <- sum(is.na(proxiesTemp$snp_genotyped))
        genotypedTemp <- sum(!is.na(proxiesTemp$snp_genotyped) & proxiesTemp$snp_genotyped == 1)
        wellImputedTemp <- sum(!is.na(proxiesTemp$snp_genotyped) & proxiesTemp$snp_genotyped == 0 & proxiesTemp$snp_score > 0.7)
        badImputedTemp <- sum(!is.na(proxiesTemp$snp_genotyped) & proxiesTemp$snp_genotyped == 0 & proxiesTemp$snp_score <= 0.7)
        
        missingWeight <- sum(proxiesTemp$weightLoss[is.na(proxiesTemp$snp_genotyped)])
        genotypedWeight <- sum(proxiesTemp$weightLoss[!is.na(proxiesTemp$snp_genotyped) & proxiesTemp$snp_genotyped == 1])
        wellImputedWeight <- sum(proxiesTemp$weightLoss[!is.na(proxiesTemp$snp_genotyped) & proxiesTemp$snp_genotyped == 0 & proxiesTemp$snp_score > 0.7])
        badImputedWeight <- sum(proxiesTemp$weightLoss[!is.na(proxiesTemp$snp_genotyped) & proxiesTemp$snp_genotyped == 0 & proxiesTemp$snp_score <= 0.7])
        
        totalWeight <- sum(proxiesTemp$weightLoss)
        
        missingWeight <- 100.0 * missingWeight / totalWeight
        genotypedWeight <- 100.0 * genotypedWeight / totalWeight
        wellImputedWeight <- 100.0 * wellImputedWeight / totalWeight
        badImputedWeight <- 100.0 * badImputedWeight / totalWeight
        
        nSnps <- c(nSnps, genotypedTemp)
        weightLosses <- c(weightLosses, genotypedWeight)
        genotyped <- c(genotyped, "Genotyped")
        cohorts <- c(cohorts, cohort)
        category <- c(category, "SNP")
        
        nSnps <- c(nSnps, wellImputedTemp)
        weightLosses <- c(weightLosses, wellImputedWeight)
        genotyped <- c(genotyped, "Imputed")
        cohorts <- c(cohorts, cohort)
        category <- c(category, "SNP")
        
        nSnps <- c(nSnps, badImputedTemp)
        weightLosses <- c(weightLosses, badImputedWeight)
        genotyped <- c(genotyped, "Poorly Imputed")
        cohorts <- c(cohorts, cohort)
        category <- c(category, "SNP")
        
        nSnps <- c(nSnps, missingTemp)
        weightLosses <- c(weightLosses, missingWeight)
        genotyped <- c(genotyped, "Missing")
        cohorts <- c(cohorts, cohort)
        category <- c(category, "SNP")
        
        missingTemp <- sum(is.na(proxiesTemp$proxy_genotyped))
        genotypedTemp <- sum(!is.na(proxiesTemp$proxy_genotyped) & proxiesTemp$proxy_genotyped == 1)
        wellImputedTemp <- sum(!is.na(proxiesTemp$proxy_genotyped) & proxiesTemp$proxy_genotyped == 0 & proxiesTemp$proxy_score > 0.7)
        badImputedTemp <- sum(!is.na(proxiesTemp$proxy_genotyped) & proxiesTemp$proxy_genotyped == 0 & proxiesTemp$proxy_score <= 0.7)
        
        missingWeight <- sum(proxiesTemp$weightLoss[is.na(proxiesTemp$proxy_genotyped)])
        genotypedWeight <- sum(proxiesTemp$weightLoss[!is.na(proxiesTemp$proxy_genotyped) & proxiesTemp$proxy_genotyped == 1])
        wellImputedWeight <- sum(proxiesTemp$weightLoss[!is.na(proxiesTemp$proxy_genotyped) & proxiesTemp$proxy_genotyped == 0 & proxiesTemp$proxy_score > 0.7])
        badImputedWeight <- sum(proxiesTemp$weightLoss[!is.na(proxiesTemp$proxy_genotyped) & proxiesTemp$proxy_genotyped == 0 & proxiesTemp$proxy_score <= 0.7])
        
        totalWeight <- sum(proxiesTemp$weightLoss)
        
        missingWeight <- 100.0 * missingWeight / totalWeight
        genotypedWeight <- 100.0 * genotypedWeight / totalWeight
        wellImputedWeight <- 100.0 * wellImputedWeight / totalWeight
        badImputedWeight <- 100.0 * badImputedWeight / totalWeight
        
        nSnps <- c(nSnps, genotypedTemp)
        weightLosses <- c(weightLosses, genotypedWeight)
        genotyped <- c(genotyped, "Genotyped")
        cohorts <- c(cohorts, cohort)
        category <- c(category, "Proxy")
        
        nSnps <- c(nSnps, wellImputedTemp)
        weightLosses <- c(weightLosses, wellImputedWeight)
        genotyped <- c(genotyped, "Imputed")
        cohorts <- c(cohorts, cohort)
        category <- c(category, "Proxy")
        
        nSnps <- c(nSnps, badImputedTemp)
        weightLosses <- c(weightLosses, badImputedWeight)
        genotyped <- c(genotyped, "Poorly Imputed")
        cohorts <- c(cohorts, cohort)
        category <- c(category, "Proxy")
        
        nSnps <- c(nSnps, missingTemp)
        weightLosses <- c(weightLosses, missingWeight)
        genotyped <- c(genotyped, "Missing")
        cohorts <- c(cohorts, cohort)
        category <- c(category, "Proxy")
        
        nMax <- max(nMax, nrow(proxiesTemp))
        
    }
    
    plotDF <- data.frame(cohort = cohorts, genotyped, category, n = nSnps, weightLoss = weightLosses, stringsAsFactors = F)
    plotDF$cohort <- factor(plotDF$cohort, levels = c("harvest", "rotterdam", "mody"))
    plotDF$genotyped <- factor(plotDF$genotyped, levels = c("Missing", "Poorly Imputed", "Imputed", "Genotyped"))
    plotDF$category <- factor(plotDF$category, levels = c("SNP", "Proxy"))
    
    
    plot <- ggplot() + theme_bw()
    
    plot <- plot + geom_col(data = plotDF, aes(x = category, y = n, col = genotyped, fill = genotyped))
    
    plot <- plot + scale_color_manual(values = c("yellow3", "orange3", "darkred", "black"))
    plot <- plot + scale_fill_manual(values = genotypingColors)
    
    plot <- plot + scale_y_continuous(name = "# markers", expand = c(0, 0), limits = c(0, 1.05 * nMax))
    
    plot <- plot + facet_grid(. ~ cohort)
    
    plot <- plot + theme(legend.position = "top",
                         legend.title = element_blank(),
                         axis.title.x = element_blank())
    
    
    png(file.path(plotFolder, paste0("genotyping_", score, ".png")), height = 8, width = 12, units = "cm", res = 300)
    plot(plot)
    dummy <- dev.off()
    
    pdf(file.path(plotFolder, paste0("genotyping_", score, ".pdf")))
    plot(plot)
    dummy <- dev.off()
    
    
    plot <- ggplot() + theme_bw()
    
    plot <- plot + geom_col(data = plotDF, aes(x = category, y = weightLoss, col = genotyped, fill = genotyped))
    
    plot <- plot + scale_color_manual(values = c("yellow3", "orange3", "darkred", "black"))
    plot <- plot + scale_fill_manual(values = genotypingColors)
    
    plot <- plot + scale_y_continuous(name = "Max weight loss [%]", expand = c(0, 0), limits = c(0, 105))
    
    plot <- plot + facet_grid(. ~ cohort)
    
    plot <- plot + theme(legend.position = "top",
                         legend.title = element_blank(),
                         axis.title.x = element_blank())
    
    
    png(file.path(plotFolder, paste0("genotyping_", score, "_weight.png")), height = 8, width = 12, units = "cm", res = 300)
    plot(plot)
    dummy <- dev.off()
    
    pdf(file.path(plotFolder, paste0("genotyping_", score, "_weight.pdf")))
    plot(plot)
    dummy <- dev.off()
    
}


## Imputation score

print(paste(Sys.time(), " Exporting imputation scores", sep = ""))

originalScores <- c()
proxyScores <- c()
cohorts <- c()
scores <- c()

for (score in scoreLevels) {
    
    for (cohort in cohortLevels) {
        
        proxiesTemp <- proxies[proxies$cohort == cohort 
                               & proxies$score == score 
                               & proxies$proxy_genotyped == 0
                               & !is.na(proxies$proxy_score), ]
        
        if (nrow(proxiesTemp) == 0) {
            stop("No proxies found")
        }
        
        proxiesTemp$snp_score[is.na(proxiesTemp$snp_score)] <- 0
        
        originalScores <- c(originalScores, proxiesTemp$snp_score)
        proxyScores <- c(proxyScores, proxiesTemp$proxy_score)
        cohorts <- c(cohorts, rep(cohort, nrow(proxiesTemp)))
        scores <- c(scores, rep(score, nrow(proxiesTemp)))
        
    }
}

plotDF <- data.frame(originalScore = originalScores, proxyScore = proxyScores, cohort = cohorts, scores = scores, stringsAsFactors = F)
plotDF$cohort <- factor(plotDF$cohort, levels = c("harvest", "rotterdam", "mody"))
plotDF$score <- factor(plotDF$score, levels = c("T2D_Diamant", "T2D_Diagram", "T1D_Oram"))
plotDF <- plotDF[order(plotDF$score), ]

plot <- ggplot() + theme_bw()

plot <- plot + geom_point(data = plotDF, aes(x = originalScore, y = proxyScore, col = score, shape = score))

plot <- plot + geom_hline(yintercept = 0.7, col = "black", linetype = "dotted")

plot <- plot + scale_color_manual(values = c("#8da0cb", "#66c2a5", "#fc8d62"))

plot <- plot + scale_x_continuous(name = "Original", breaks = (0:5)/5)
plot <- plot + scale_y_continuous(name = "Proxy", breaks = (0:5)/5)

plot <- plot + facet_grid(. ~ cohort)

plot <- plot + theme(legend.position = "top",
                     legend.title = element_blank(),
                     panel.grid.minor = element_blank())


png(file.path(plotFolder, paste0("imputation_score.png")), height = 8, width = 12, units = "cm", res = 300)
plot(plot)
dummy <- dev.off()

pdf(file.path(plotFolder, paste0("imputation_score.pdf")))
plot(plot)
dummy <- dev.off()



