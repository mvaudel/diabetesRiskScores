# 
# This script plots the grs estimated in the different cohorts.
#

# Libraries

library(ggplot2)
library(scico)


# Parameters

## Input files
harvestScoreFile <- "resources/grs/harvest_scores.gz"
rotterdamScoreFile <- "resources/grs/rotterdam_scores.gz"
modyScoreFile <- "resources/grs/mody_scores.gz"


## Doc files
plotFolder <- "docs/grs"


# Functions

getQuantile <- function(value, scoreQuantiles) {
    
    for (i in 1:length(scoreQuantiles)) {
        
        scoreQuantile <- scoreQuantiles[i]
        
        if (value < scoreQuantile) {
            
            return((i-0.5)/10)
            
        }
    }
    
    return((length(scoreQuantiles)+0.5)/10)
    
}



# Main script

## Load data

print(paste(Sys.time(), " Loading data", sep = ""))

harvestScore <- read.table(harvestScoreFile, header = T, stringsAsFactors = F)
rotterdamScore <- read.table(rotterdamScoreFile, header = T, stringsAsFactors = F)
modyScore <- read.table(modyScoreFile, header = T, stringsAsFactors = F)


## Formatting

print(paste(Sys.time(), " Formatting", sep = ""))

harvestScore$cohort <- "Background"
rotterdamScore$cohort <- "Background"
modyScore$cohort <- "MODY"

harvestScore$status <- harvestScore$category
rotterdamScore$status <- rotterdamScore$category
modyScore$status <- ifelse(modyScore$category == "MODYX", "MODYX", "MODY")

scoresDF <- rbind(harvestScore, rotterdamScore, modyScore)

scoresDF$cohort <- factor(scoresDF$cohort)
cohortLevels <- levels(scoresDF$cohort)

scoresDF$category <- factor(scoresDF$category)
categoryLevels <- levels(scoresDF$category)

scoresDF$sex <- factor(scoresDF$sex)
sexLevels <- levels(scoresDF$sex)

scoresDF$status[scoresDF$status == "not_reported" & !is.na(scoresDF$role) & scoresDF$role == "Kid"] <- "not_reported_kid"
scoresDF$status[scoresDF$status == "not_reported" & !is.na(scoresDF$role) & scoresDF$role != "Kid"] <- "not_reported_parent"

scoresDF$status <- factor(scoresDF$status)
statusLevels <- levels(scoresDF$status)

scoreColumns <- c("T1dOram", "T2dDiamant", "T2dDiagram")


## Normalize scores

print(paste(Sys.time(), " Normalization", sep = ""))

scoresDF$normalizedValue <- NA

for (scoreColumn in scoreColumns) {
        
    backgroundValues <- scoresDF[scoresDF$cohort == "Background" & scoresDF$status == "not_reported_parent", scoreColumn]
    
    quantiles <- quantile(backgroundValues, probs = (0:1000)/1000)
    
    newColumn <- paste0(scoreColumn, "_normalized")
    
    scoresDF[, newColumn] <- sapply(X = scoresDF[, scoreColumn], FUN = getQuantile, scoreQuantiles = quantiles)
        
}


# Elementary vectors

palette <- 'roma'
t1dColor <- scico(n = 1, begin = 0.15, end = 0.15, palette = palette)
t2dColor <- scico(n = 1, begin = 0.85, end = 0.85, palette = palette)

mody1 <- c()
mody2 <- c()
t1d1 <- matrix(nrow = 10, ncol = 10)
t1d2 <- matrix(nrow = 10, ncol = 10)

x <- c()
y <- c()
categories <- c()
scores <- c()

k <- 1

for (status in statusLevels) {
    
    if (status != "other_gestational") {
        
        categoryDF <- scoresDF[scoresDF$status == status, ]
        
        for (i in 1:10) {
            
            binLow <- 10.0 * (i-1)
            binHigh <- 10.0 * i
            binCenter <- 10.0 * (i - 0.5)
            
            x[k] <- binCenter
            y[k] <- -100.0 * sum(categoryDF$T1dOram_normalized > binLow & categoryDF$T1dOram_normalized <= binHigh) / nrow(categoryDF)
            categories[k] <- status
            scores[k] <- "T1D"
            
            k <- k + 1
            
            x[k] <- binCenter
            y[k] <- 100.0 * sum(categoryDF$T2dDiagram_normalized > binLow & categoryDF$T2dDiagram_normalized <= binHigh) / nrow(categoryDF)
            categories[k] <- status
            scores[k] <- "T2D"
            
            k <- k + 1
            
        }
    }
}

maxcOcurrence <- max(abs(y))

plotDF <- data.frame(x, y, status = categories, score = scores, stringsAsFactors = F)
plotDF$score <- factor(plotDF$score, levels = c("T2D", "T1D"))
plotDF$status <- factor(plotDF$status, levels = c("not_reported_parent", "not_reported_kid", "diabetes", "childhood", "type_1", "type_2", "gestational", "MODY", "MODYX"))
levels(plotDF$status) <- c("Not Reported Parent", "Not Reported kid", "Diabetes", "Childhood", "Type 1", "Type 2", "Gestational", "MODY", "MODYX")

plot <- ggplot() + theme_bw()

plot <- plot + geom_col(data = plotDF, aes(x = x, y = y, fill = score, col = score), alpha = 0.8)

plot <- plot + scale_fill_manual(values = c(t2dColor, t1dColor))
plot <- plot + scale_color_manual(values = c(t2dColor, t1dColor))

plot <- plot + coord_flip()
plot <- plot + scale_y_continuous(breaks = c(-20, -10, 0, 10, 20), labels = c(20, 10, 0, 10, 20), limits = c(-maxcOcurrence, maxcOcurrence))

plot <- plot + xlab("Normalized Score") + ylab("# Patients [%]")

plot <- plot + theme(legend.position = "top",
                     legend.title = element_blank())

plot <- plot + facet_grid(status ~ .)

png("docs/grs/hist.png", height = 48, width = 8, units = "cm", res = 300)
plot(plot)
dummy <- dev.off()

pdf("docs/grs/hist.pdf", height = unit(24, "cm"), width = unit(4, "cm"))
plot(plot)
dummy <- dev.off()


# Plot individual scores against expected score

expectedScores <- c()
actualScores <- c()
categories <- c()
scores <- c()

for (status in statusLevels) {
    
    if (status != "other_gestational") {
        
        categoryDF <- scoresDF[scoresDF$status == status, ]
        n <- nrow(categoryDF)
        expectedScore <- (1:n) / n * 100.0
        
        categoryDF <- categoryDF[order(categoryDF$T1dOram_normalized), ]
        expectedScores <- c(expectedScores, expectedScore)
        actualScores <- c(actualScores, categoryDF$T1dOram_normalized)
        categories <- c(categories, rep(status, n))
        scores <- c(scores, rep("T1D", n))
        
        categoryDF <- categoryDF[order(categoryDF$T2dDiagram_normalized), ]
        expectedScores <- c(expectedScores, expectedScore)
        actualScores <- c(actualScores, categoryDF$T2dDiagram_normalized)
        categories <- c(categories, rep(status, n))
        scores <- c(scores, rep("T2D", n))
        
    }
}

plotDF <- data.frame(x = expectedScores, y = actualScores, status = categories, score = scores, stringsAsFactors = F)
plotDF$score <- factor(plotDF$score)
plotDF$status <- factor(plotDF$status, levels = c("not_reported_parent", "not_reported_kid", "diabetes", "childhood", "type_1", "type_2", "gestational", "MODY", "MODYX"))
levels(plotDF$status) <- c("Not Reported Parent", "Not Reported kid", "Diabetes", "Childhood", "Type 1", "Type 2", "Gestational", "MODY", "MODYX")

backgroundDF <- scoresDF[scoresDF$status == "not_reported_parent", ]

qI <- c()
q99 <- c()
q95 <- c()
q05 <- c()
q01 <- c()
qScores <- c()

for (i in 1:100) {
    
    t1dI <- c()
    t2dI <- c()
    
    for (j in 1:100) {
        
        sampledDF <- backgroundDF[sample(1:nrow(backgroundDF), size = 100), ]
        
        sampledDF <- sampledDF[order(sampledDF$T1dOram_normalized), ]
        t1dI[j] <- sampledDF$T1dOram_normalized[i]
        
        sampledDF <- sampledDF[order(sampledDF$T2dDiagram_normalized), ]
        t2dI[j] <- sampledDF$T2dDiagram_normalized[i]
        
    }
    
    t1dI <- t1dI[order(t1dI)]
    t2dI <- t2dI[order(t2dI)]
    
    qI <- c(qI, i, i)
    q99 <- c(q99, t1dI[99], t2dI[99])
    q95 <- c(q95, t1dI[95], t2dI[95])
    q05 <- c(q05, t1dI[5], t2dI[5])
    q01 <- c(q01, t1dI[1], t2dI[1])
    qScores <- c(qScores, "T1D", "T2D")
    
}

qDF <- data.frame(qI, q01, q05, q95, q99, score = qScores)

plot <- ggplot() + theme_bw()

plot <- plot + geom_abline(intercept = 0, slope = 1, linetype = "solid", color = "darkgray")

plot <- plot + geom_line(data = qDF, aes(x = qI, y = q01), linetype = "dotted", color = "darkgreen", alpha = 0.5)
plot <- plot + geom_line(data = qDF, aes(x = qI, y = q99), linetype = "dotted", color = "darkgreen", alpha = 0.5)
plot <- plot + geom_line(data = qDF, aes(x = qI, y = q05), linetype = "solid", color = "darkgreen", alpha = 0.5)
plot <- plot + geom_line(data = qDF, aes(x = qI, y = q95), linetype = "solid", color = "darkgreen", alpha = 0.5)

plot <- plot + geom_point(data = plotDF, aes(x = x, y = y, col = score), alpha = 0.8)

plot <- plot + scale_color_manual(values = c(t1dColor, t2dColor))

plot <- plot + scale_x_continuous(name = "Expected score", breaks = 20 * (0:5), limits = c(-5, 105), expand = c(0, 0))
plot <- plot + scale_y_continuous(name = "Actual score", breaks = 20 * (0:5), limits = c(-5, 105), expand = c(0, 0))

plot <- plot + facet_grid(status ~ score)

png("docs/grs/pp.png", height = 48, width = 16, units = "cm", res = 300)
plot(plot)
dummy <- dev.off()

pdf("docs/grs/pp.pdf", height = unit(24, "cm"), width = unit(8, "cm"))
plot(plot)
dummy <- dev.off()


# Infer probability of being mody

t1DF <- scoresDF[scoresDF$status == "type_1", ]

t1DF <- t1DF[order(t1DF$T1dOram_normalized), ]
n <- nrow(t1DF)
t1DF$x1 <- (1:n) / n * 100.0

t1DF <- t1DF[order(t1DF$T2dDiagram_normalized), ]
n <- nrow(t1DF)
t1DF$x2 <- (1:n) / n * 100.0

mDF <- scoresDF[scoresDF$status == "MODY", ]

mDF <- mDF[order(mDF$T1dOram_normalized), ]
n <- nrow(mDF)
mDF$x1 <- (1:n) / n * 100.0

mDF <- mDF[order(mDF$T2dDiagram_normalized), ]
n <- nrow(mDF)
mDF$x2 <- (1:n) / n * 100.0

xDF <- scoresDF[scoresDF$status == "MODYX", ]

xDF <- xDF[order(xDF$T1dOram_normalized), ]
n <- nrow(xDF)
xDF$x1 <- (1:n) / n * 100.0

xDF <- xDF[order(xDF$T2dDiagram_normalized), ]
n <- nrow(xDF)
xDF$x2 <- (1:n) / n * 100.0


# Plot allele prevalence in each category



