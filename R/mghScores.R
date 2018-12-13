# 
# This script plots the score distribution in the mgh cohort.
#

# Libraries

library(ggplot2)
library(ggforce)
library(scico)


# Parameters

## Input files
scoresFile <- "C:\\Projects\\mgh\\updated_with_21Kdataset_Oram_T1D-GRS.gz"
phenoFile <- "C:\\Projects\\mgh\\T1D_curated_set_2018_08_14_reduced.csv"


## Output folder
plotFolder <- "C:\\Projects\\mgh\\plots"


## ggplot theme
theme_set(theme_bw(base_size = 11))


## Colors

palette <- 'cork'
t1dColor <- scico(n = 1, begin = 0.15, end = 0.15, palette = palette)
controlColor <- scico(n = 1, begin = 0.85, end = 0.85, palette = palette)


# Functions

#' Returns the id from a plink sample name.
#' 
#' @param sample the plink sample name
#' 
#' @return the sample id
getId <- function(sample) {
    
    startI <- regexpr(sample, pattern = "-") + 1
    endI <- regexpr(sample, pattern = "_") - 1
    
    id <- substr(sample, start = startI, stop = endI)
    
    return(id)
    
}


# Main script

## Load data

print(paste(Sys.time(), " Loading data", sep = ""))

phenoDF <- read.table(phenoFile, header = T, sep = ",", stringsAsFactors = F)
names(phenoDF) <- c("id", "t1d", "Control")


## Format identifiers

scoresDF$id <- sapply(X = scoresDF$Sample, FUN = getId)


## Merge

mergedDF <- merge(x = phenoDF, y = scoresDF, by = "id", all = F)


## Plot score distribution

scoreDistribution <- ggplot()

scoreDistribution <- scoreDistribution + geom_violin(data = mergedDF, mapping = aes(x = Control, y = Score, fill = Control, col = Control), alpha = 0.5, width = 0.5)
scoreDistribution <- scoreDistribution + geom_sina(data = mergedDF, mapping = aes(x = Control, y = Score, col = Control, size = Control), alpha = 0.2, scale = F, maxwidth = 1)

scoreDistribution <- scoreDistribution + scale_fill_manual(values = c(t1dColor, controlColor))
scoreDistribution <- scoreDistribution + scale_color_manual(values = c(t1dColor, controlColor))
scoreDistribution <- scoreDistribution + scale_size_manual(values = c(2, 0.5))

scoreDistribution <- scoreDistribution + ylab("T1D Score")

png(filename = file.path(plotFolder, "scores.png"), width = 600, height = 800)
plot(scoreDistribution)
dummy <- dev.off()
