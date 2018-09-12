# 
# This script plots the grs estimated in the different cohorts.
#

anonymized <- F

# Libraries

if (!anonymized) {
    library(digest, lib="~/R")
    library(reshape2, lib="~/R")
    library(labeling, lib="~/R")
    library(ggplot2, lib="~/R")
    library(scico, lib="~/R")
    library(ggrepel, lib="~/R")
} else {
    library(ggplot2)
    library(scico)
    library(ggrepel)
}


# Parameters

## Input files
if (!anonymized) {
    harvestScoreFile <- "/mnt/archive/mody/grs/harvest_scores.gz"
    rotterdamScoreFile <- "/mnt/archive/mody/grs/rotterdam_scores.gz"
    modyScoreFile <- "/mnt/archive/mody/grs/mody_scores.gz"
} else {
    harvestScoreFile <- "resources/grs/harvest_scores.gz"
    rotterdamScoreFile <- "resources/grs/rotterdam_scores.gz"
    modyScoreFile <- "resources/grs/mody_scores.gz"
}

## mody risk file
modyxFile <- "/mnt/archive/mody/grs/modyX"

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
    
    return((length(scoreQuantiles)-0.5)/10)
    
}



# Main script

## Load data

print(paste(Sys.time(), " Loading data", sep = ""))

harvestScore <- read.table(harvestScoreFile, header = T, stringsAsFactors = F)
rotterdamScore <- read.table(rotterdamScoreFile, header = T, stringsAsFactors = F)
modyScore <- read.table(modyScoreFile, header = T, stringsAsFactors = F)


## Formatting scores

print(paste(Sys.time(), " Formatting", sep = ""))

harvestScore$cohort <- "Background"
rotterdamScore$cohort <- "Background"
modyScore$cohort <- "MODY"

harvestScore$status <- harvestScore$category
rotterdamScore$status <- rotterdamScore$category
modyScore$status <- modyScore$category

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
    
    quantiles <- quantile(backgroundValues, probs = (1:1000)/1000)
    
    newColumn <- paste0(scoreColumn, "_normalized")
    
    scoresDF[, newColumn] <- sapply(X = scoresDF[, scoreColumn], FUN = getQuantile, scoreQuantiles = quantiles)
    
}


# Histogram of the different scores

palette <- 'roma'
t1dColor <- scico(n = 1, begin = 0.15, end = 0.15, palette = palette)
t2dColor <- scico(n = 1, begin = 0.85, end = 0.85, palette = palette)
t1dColorLight <- scico(n = 1, begin = 0.35, end = 0.35, palette = palette)
t2dColorLight <- scico(n = 1, begin = 0.65, end = 0.65, palette = palette)

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
            y[k] <- 100.0 * sum(categoryDF$T1dOram_normalized > binLow & categoryDF$T1dOram_normalized <= binHigh) / nrow(categoryDF)
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

histDF <- data.frame(x, y, status = categories, score = scores, stringsAsFactors = F)

plotDF <- histDF
plotDF$y[plotDF$score == "T1D"] <- -plotDF$y[plotDF$score == "T1D"]
plotDF$score <- factor(plotDF$score, levels = c("T2D", "T1D"))
plotDF$status <- factor(plotDF$status, levels = c("not_reported_parent", "not_reported_kid", "diabetes", "childhood", "type_1", "type_2", "gestational", "MODY", "MODYX"))
levels(plotDF$status) <- c("Not Reported Parent", "Not Reported Kid", "Father Diabetes", "Childhood", "Mother Type 1", "Mother Type 2", "Gestational", "MODY", "MODYX")

breaks <- 10 * (-6:6)

plot <- ggplot() + theme_bw()

plot <- plot + geom_col(data = plotDF, aes(x = x, y = y, fill = score, col = score), alpha = 0.8)

plot <- plot + geom_hline(yintercept = 0, color = "black")

plot <- plot + scale_fill_manual(values = c(t2dColor, t1dColor))
plot <- plot + scale_color_manual(values = c(t2dColor, t1dColor))

plot <- plot + coord_flip()
plot <- plot + scale_y_continuous(breaks = breaks, labels = abs(breaks), limits = c(-maxcOcurrence, maxcOcurrence))

plot <- plot + xlab("Normalized Score") + ylab("# Patients [%]")

plot <- plot + theme(legend.position = "top",
                     legend.title = element_blank(),
                     strip.background =element_rect(fill="grey95"))

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

ppDF <- data.frame(x = expectedScores, y = actualScores, status = categories, score = scores, stringsAsFactors = F)

plotDF <- data.frame(x = expectedScores, y = actualScores, status = categories, score = scores, stringsAsFactors = F)

plotDF$score <- factor(plotDF$score)
plotDF$status <- factor(plotDF$status, levels = c("not_reported_parent", "not_reported_kid", "diabetes", "childhood", "type_1", "type_2", "gestational", "MODY", "MODYX"))
levels(plotDF$status) <- c("Not Reported Parent", "Not Reported Kid", "Father Diabetes", "Childhood", "Mother Type 1", "Mother Type 2", "Gestational", "MODY", "MODYX")

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

plot <- plot + theme(strip.background =element_rect(fill="grey95"),
                     legend.position = "none")

plot <- plot + facet_grid(status ~ score)

png("docs/grs/pp.png", height = 48, width = 16, units = "cm", res = 300)
plot(plot)
dummy <- dev.off()

pdf("docs/grs/pp.pdf", height = unit(24, "cm"), width = unit(8, "cm"))
plot(plot)
dummy <- dev.off()


# Plot mody and modyX histogram with outliers highlighted

nMody <- sum(scoresDF$status == "MODY")

mHistDF <- histDF[histDF$status == "MODY", ]
mHistDF$y <- mHistDF$y * nMody / 100

mT1dCountLow <- mHistDF$y[mHistDF$score == "T1D" & mHistDF$x <= 80]
m1Quantiles <- quantile(mT1dCountLow, probs = c(pnorm(-1), 0.5), names = F)
m1Median <- m1Quantiles[2]
m1Lim <- m1Quantiles[2] + 1.96 * (m1Quantiles[2] - m1Quantiles[1])

mT2dCountLow <- mHistDF$y[mHistDF$score == "T2D" & mHistDF$x <= 80]
m2Quantiles <- quantile(mT2dCountLow, probs = c(pnorm(-1), 0.5), names = F)
m2Median <- m2Quantiles[2]
m2Lim <- m2Quantiles[2] + 1.96 * (m2Quantiles[2] - m2Quantiles[1])

plotDF <- mHistDF
plotDF$y[plotDF$score == "T1D"] <- -plotDF$y[plotDF$score == "T1D"]
plotDF$score <- factor(plotDF$score, levels = c("T2D", "T1D"))

maxcOcurrence <- max(abs(plotDF$y), m1Lim, m2Lim)

breaks <- c(10 * (-2:2), round(-m1Lim, digits = 1), round(-m1Median, digits = 1), round(m2Median, digits = 1), round(m2Lim, digits = 1))
labels <- abs(breaks)
breakColors <- c(rep("black", 5), t1dColor, t1dColor, t2dColor, t2dColor)

plot <- ggplot() + theme_bw()

plot <- plot + geom_col(data = plotDF, aes(x = x, y = y, fill = score, col = score), alpha = 0.8)

plot <- plot + geom_hline(yintercept = 0, color = "black")
plot <- plot + geom_hline(yintercept = -m1Median, color = t1dColor, linetype = "dashed")
plot <- plot + geom_hline(yintercept = -m1Lim, color = t1dColor, linetype = "dotted")
plot <- plot + geom_hline(yintercept = m2Median, color = t2dColor, linetype = "dashed")
plot <- plot + geom_hline(yintercept = m2Lim, color = t2dColor, linetype = "dotted")

plot <- plot + scale_fill_manual(values = c(t2dColor, t1dColor))
plot <- plot + scale_color_manual(values = c(t2dColor, t1dColor))

plot <- plot + coord_flip()
plot <- plot + scale_y_continuous(breaks = breaks, labels = abs(breaks), limits = c(-maxcOcurrence, maxcOcurrence))

plot <- plot + xlab("Normalized Score") + ylab("# Patients")

plot <- plot + theme(legend.position = "top",
                     legend.title = element_blank(),
                     axis.text.x = element_text(color = breakColors),
                     panel.grid.minor.x = element_blank(),
                     strip.background =element_rect(fill="grey95"))

png("docs/grs/hist_mody.png", height = 18, width = 27, units = "cm", res = 300)
plot(plot)
dummy <- dev.off()

pdf("docs/grs/hist_mody.pdf", height = unit(9, "cm"), width = unit(13.5, "cm"))
plot(plot)
dummy <- dev.off()


nModyX <- sum(scoresDF$status == "MODYX")

xHistDF <- histDF[histDF$status == "MODYX", ]
xHistDF$y <- xHistDF$y * nModyX / 100

xT1dCountLow <- xHistDF$y[xHistDF$score == "T1D" & xHistDF$x <= 80]
x1Quantiles <- quantile(xT1dCountLow, probs = c(pnorm(-1), 0.5), names = F)
x1Median <- x1Quantiles[2]
x1Lim <- x1Quantiles[2] + 1.96 * (x1Quantiles[2] - x1Quantiles[1])

xT2dCountLow <- xHistDF$y[xHistDF$score == "T2D" & xHistDF$x <= 80]
x2Quantiles <- quantile(xT2dCountLow, probs = c(pnorm(-1), 0.5), names = F)
x2Median <- x2Quantiles[2]
x2Lim <- x2Quantiles[2] + 1.96 * (x2Quantiles[2] - x2Quantiles[1])

plotDF <- xHistDF
plotDF$y[plotDF$score == "T1D"] <- -plotDF$y[plotDF$score == "T1D"]
plotDF$score <- factor(plotDF$score, levels = c("T2D", "T1D"))

maxcOcurrence <- max(abs(plotDF$y))

breaks <- c(10 * (-6:6), round(-x1Lim, digits = 1), round(-x1Median, digits = 1), round(x2Median, digits = 1), round(x2Lim, digits = 1))
labels <- abs(breaks)
breakColors <- c(rep("black", 13), t1dColor, t1dColor, t2dColor, t2dColor)

plot <- ggplot() + theme_bw()

plot <- plot + geom_col(data = plotDF, aes(x = x, y = y, fill = score, col = score), alpha = 0.8)

plot <- plot + geom_hline(yintercept = 0, color = "black")
plot <- plot + geom_hline(yintercept = -x1Median, color = t1dColor, linetype = "dashed")
plot <- plot + geom_hline(yintercept = -x1Lim, color = t1dColor, linetype = "dotted")
plot <- plot + geom_hline(yintercept = x2Median, color = t2dColor, linetype = "dashed")
plot <- plot + geom_hline(yintercept = x2Lim, color = t2dColor, linetype = "dotted")

plot <- plot + scale_fill_manual(values = c(t2dColor, t1dColor))
plot <- plot + scale_color_manual(values = c(t2dColor, t1dColor))

plot <- plot + coord_flip()
plot <- plot + scale_y_continuous(breaks = breaks, labels = abs(breaks), limits = c(-maxcOcurrence, maxcOcurrence))

plot <- plot + xlab("Normalized Score") + ylab("# Patients")

plot <- plot + theme(legend.position = "top",
                     legend.title = element_blank(),
                     axis.text.x = element_text(color = breakColors),
                     panel.grid.minor.x = element_blank(),
                     strip.background =element_rect(fill="grey95"))

png("docs/grs/hist_modyx.png", height = 18, width = 27, units = "cm", res = 300)
plot(plot)
dummy <- dev.off()

pdf("docs/grs/hist_modyx.pdf", height = unit(9, "cm"), width = unit(13.5, "cm"))
plot(plot)
dummy <- dev.off()


# Stratify scores according to t1d and t2d bins

xdDF <- scoresDF[scoresDF$status == "MODYX", ]

xMatrix <- matrix(nrow = 10, ncol = 10)

for (i in 1:10) {
    
    binILow <- 10.0 * (i-1)
    binIHigh <- 10.0 * i
    
    for (j in 1:10) {
        
        binJLow <- 10.0 * (j-1)
        binJHigh <- 10.0 * j
        
        nIJ <- sum(xdDF$T1dOram_normalized > binILow & xdDF$T1dOram_normalized <= binIHigh
                   & xdDF$T2dDiagram_normalized > binJLow & xdDF$T2dDiagram_normalized <= binJHigh)
        
        xMatrix[i, j] <- nIJ
        
    }
}


# Estimate the probability for mody x patients to be T1D

xBins <- 10 * ((1:10) - 0.5)

xT1dCount <- xHistDF$y[xHistDF$score == "T1D"]

xMody <- ifelse(xT1dCount > x1Lim, x1Median, xT1dCount)
xT1D <- xT1dCount - xMody

pMatrix <- matrix(nrow = 10, ncol = 10)

for (i in 1:10) {
    
    expectedT2DI <- xT1D[i] / 10
    
    delay = 0
    
    for (j in 10:1) {
        
        found = xMatrix[i, j]
        needed <- delay + expectedT2DI
        
        if (found < needed) {
            
            pMatrix[i, j] <- 1
            delay = needed - found
            
        } else if (found > 0) {
            
            pMatrix[i, j] <- needed / found
            delay <- 0
            
        } else {
            
            pMatrix[i, j] <- 0
            
        }
    }
}

t2dShare <- sum(xT1D) / 10

x <- c()
y <- c()
score <- c()

tempDF <- xHistDF[xHistDF$score == "T1D" & xHistDF$x < 90, ]

x <- c(x, tempDF$x)
y <- c(y, -tempDF$y)
score <- c(score, rep("Low T1D", nrow(tempDF)))

tempDF <- xHistDF[xHistDF$score == "T1D" & xHistDF$x > 90, ]

x <- c(x, tempDF$x)
y <- c(y, -tempDF$y)
score <- c(score, rep("High T1D", nrow(tempDF)))

x <- c(x, xBins)
y <- c(y, xMatrix[10, ])
score <- c(score, rep("T2D - High T1D", 10))

x <- c(x, xBins)
y <- c(y, apply(X = xMatrix[1:9, ], FUN = sum, MARGIN = 2))
score <- c(score, rep("T2D - Low T1D", 10))

plotDF <- data.frame(x, y, score, stringsAsFactors = F)
plotDF$score <- factor(plotDF$score, levels = c("Low T1D", "High T1D", "T2D - Low T1D", "T2D - High T1D"))

breaks <- c(10 * (-6:6), round(-x1Lim, digits = 1), round(-x1Median, digits = 1), round(t2dShare, digits = 1))
labels <- abs(breaks)
labels[5] <- ""
breakColors <- c(rep("black", 13), t1dColor, t1dColor, t2dColor)

maxcOcurrence <- max(abs(histDF$y[histDF$status == "MODYX"] * nModyX / 100))

plot <- ggplot() + theme_bw()

plot <- plot + geom_col(data = plotDF, aes(x = x, y = y, fill = score, col = score), alpha = 0.8)

plot <- plot + geom_hline(yintercept = 0, color = "black")
plot <- plot + geom_hline(yintercept = -x1Median, color = t1dColor, linetype = "dashed")
plot <- plot + geom_hline(yintercept = -x1Lim, color = t1dColor, linetype = "dotted")
plot <- plot + geom_hline(yintercept = t2dShare, color = t2dColor, linetype = "dashed")

plot <- plot + scale_fill_manual(values = c(t1dColorLight, t1dColor, t2dColorLight, t2dColor))
plot <- plot + scale_color_manual(values = c(t1dColorLight, t1dColor, t2dColorLight, t2dColor))

plot <- plot + coord_flip()
plot <- plot + scale_x_continuous(breaks = 20*(0:5))
plot <- plot + scale_y_continuous(breaks = breaks, labels = labels, limits = c(-maxcOcurrence, maxcOcurrence))

plot <- plot + xlab("Normalized Score") + ylab("# Patients")

plot <- plot + theme(legend.position = "top",
                     legend.title = element_blank(),
                     panel.grid.minor.x = element_blank(),
                     axis.text.x = element_text(color = breakColors))

png("docs/grs/modyX_T1D_hist.png", height = 18, width = 27, units = "cm", res = 300)
plot(plot)
dummy <- dev.off()

pdf("docs/grs/modyX_T1D_hist.pdf", height = unit(9, "cm"), width = unit(13.5, "cm"))
plot(plot)
dummy <- dev.off()


modyXMatrix <- scoresDF[scoresDF$status == "MODYX", ]
modyXMatrix$pT1d <- NA

for (i in 1:10) {
    
    binILow <- 10.0 * (i-1)
    binIHigh <- 10.0 * i
    
    for (j in 1:10) {
        
        binJLow <- 10.0 * (j-1)
        binJHigh <- 10.0 * j
        
        rows <- modyXMatrix$T1dOram_normalized > binILow & modyXMatrix$T1dOram_normalized <= binIHigh & modyXMatrix$T2dDiagram_normalized > binJLow & modyXMatrix$T2dDiagram_normalized <= binJHigh
        
        modyXMatrix$pT1d[rows] <- pMatrix[i, j]

    }
}

x <- c()
y <- c()

for (i in 0:10) {
    
    binMiddle <- i/10
    binLow <- (i-0.5)/10
    binHigh <- (i+0.5)/10
    
    n <- sum(modyXMatrix$pT1d > binLow & modyXMatrix$pT1d <= binHigh)
    
    x <- c(x, binMiddle)
    y <- c(y, n)
}

plotDF <- data.frame(x, y)

plot <- ggplot() + theme_bw()

plot <- plot + geom_col(data = plotDF, aes(x = x, y = y, fill = x), col = "grey30") 

plot <- plot + scale_fill_scico(palette = "oslo", direction = -1)

plot <- plot + scale_x_continuous(name = "T1D Probability [%]", breaks = (0:10)/10, labels = 10 * (0:10))
plot <- plot + scale_y_continuous(name = "# Samples", expand = c(0, 0), limits = c(0, 1.05 * max(plotDF$y)), breaks = 20 * (0:(round(max(plotDF$y))+1)))

plot <- plot + theme(legend.position = "none",
                     panel.grid.minor.x = element_blank())

png("docs/grs/modyX_T1D_p.png", height = 18, width = 27, units = "cm", res = 300)
plot(plot)
dummy <- dev.off()

pdf("docs/grs/modyX_T1D_p.pdf", height = unit(9, "cm"), width = unit(13.5, "cm"))
plot(plot)
dummy <- dev.off()


# Estimate the probability for mody x patients to be T2D

xT2dCount <- xHistDF$y[xHistDF$score == "T2D"]

xMody <- ifelse(xT2dCount > x2Lim, x2Median, xT2dCount)
xT2D <- xT2dCount - xMody

pMatrix <- matrix(nrow = 10, ncol = 10)

for (i in 1:10) {
    
    expectedT1DI <- xT2D[i] / 10
    
    delay = 0
    
    for (j in 10:1) {
        
        found = xMatrix[j, i]
        needed <- delay + expectedT1DI
        
        if (found < needed) {
            
            pMatrix[j, i] <- 1
            delay = needed - found
            
        } else if (found > 0) {
            
            pMatrix[j, i] <- needed / found
            delay <- 0
            
        } else {
            
            pMatrix[j, i] <- 0
            
        }
    }
}

t1dShare <- sum(xT2D) / 10

x <- c()
y <- c()
score <- c()

tempDF <- xHistDF[xHistDF$score == "T2D" & xHistDF$x < 80, ]

x <- c(x, tempDF$x)
y <- c(y, tempDF$y)
score <- c(score, rep("Low T2D", nrow(tempDF)))

tempDF <- xHistDF[xHistDF$score == "T2D" & xHistDF$x > 80, ]

x <- c(x, tempDF$x)
y <- c(y, tempDF$y)
score <- c(score, rep("High T2D", nrow(tempDF)))

x <- c(x, xBins)
y <- c(y, -apply(X = xMatrix[, 9:10], FUN = sum, MARGIN = 1))
score <- c(score, rep("T1D - High T2D", 10))

x <- c(x, xBins)
y <- c(y, -apply(X = xMatrix[, 1:8], FUN = sum, MARGIN = 1))
score <- c(score, rep("T1D - Low T1D", 10))

plotDF <- data.frame(x, y, score, stringsAsFactors = F)
plotDF$score <- factor(plotDF$score, levels = c("T1D - Low T1D", "T1D - High T2D", "Low T2D", "High T2D"))

maxcOcurrence <- max(abs(histDF$y[histDF$status == "MODYX"] * nModyX / 100))

breaks <- c(10 * (-6:6) , round(x2Lim, digits = 1), round(x2Median, digits = 1), round(-t1dShare, digits = 1))
labels <- abs(breaks)
breakColors <- c(rep("black", 13), t2dColor, t2dColor, t1dColor)

plot <- ggplot() + theme_bw()

plot <- plot + geom_col(data = plotDF, aes(x = x, y = y, fill = score, col = score), alpha = 0.8)

plot <- plot + geom_hline(yintercept = 0, color = "black")
plot <- plot + geom_hline(yintercept = x2Median, color = t2dColor, linetype = "dashed")
plot <- plot + geom_hline(yintercept = x2Lim, color = t2dColor, linetype = "dotted")
plot <- plot + geom_hline(yintercept = -t1dShare, color = t1dColor, linetype = "dashed")

plot <- plot + scale_fill_manual(values = c(t1dColorLight, t1dColor, t2dColorLight, t2dColor))
plot <- plot + scale_color_manual(values = c(t1dColorLight, t1dColor, t2dColorLight, t2dColor))

plot <- plot + coord_flip()
plot <- plot + scale_y_continuous(breaks = breaks, labels = labels, limits = c(-maxcOcurrence, maxcOcurrence))

plot <- plot + xlab("Normalized Score") + ylab("# Patients")

plot <- plot + theme(legend.position = "top",
                     legend.title = element_blank(),
                     panel.grid.minor.x = element_blank(),
                     axis.text.x = element_text(color = breakColors))

png("docs/grs/modyX_T2D_hist.png", height = 18, width = 27, units = "cm", res = 300)
plot(plot)
dummy <- dev.off()

pdf("docs/grs/modyX_T2D_hist.pdf", height = unit(9, "cm"), width = unit(13.5, "cm"))
plot(plot)
dummy <- dev.off()


modyXMatrix$pT2d <- NA

for (i in 1:10) {
    
    binILow <- 10.0 * (i-1)
    binIHigh <- 10.0 * i
    
    for (j in 1:10) {
        
        binJLow <- 10.0 * (j-1)
        binJHigh <- 10.0 * j
        
        rows <- modyXMatrix$T1dOram_normalized > binILow & modyXMatrix$T1dOram_normalized <= binIHigh & modyXMatrix$T2dDiagram_normalized > binJLow & modyXMatrix$T2dDiagram_normalized <= binJHigh
        
        modyXMatrix$pT2d[rows] <- pMatrix[i, j]
        
    }
}

x <- c()
y <- c()

for (i in 0:10) {
    
    binMiddle <- i/10
    binLow <- (i-0.5)/10
    binHigh <- (i+0.5)/10
    
    n <- sum(modyXMatrix$pT2d > binLow & modyXMatrix$pT2d <= binHigh)
    
    x <- c(x, binMiddle)
    y <- c(y, n)
}

plotDF <- data.frame(x, y)

plot <- ggplot() + theme_bw()

plot <- plot + geom_col(data = plotDF, aes(x = x, y = y, fill = x), col = "grey30") 

plot <- plot + scale_fill_scico(palette = "oslo", direction = -1)

plot <- plot + scale_x_continuous(name = "T2D Probability [%]", breaks = (0:10)/10, labels = 10 * (0:10))
plot <- plot + scale_y_continuous(name = "# Samples", expand = c(0, 0), limits = c(0, 1.05 * max(plotDF$y)), breaks = 20 * (0:(round(max(plotDF$y))+1)))

plot <- plot + theme(legend.position = "none",
                     panel.grid.minor.x = element_blank())

png("docs/grs/modyX_T2D_p.png", height = 18, width = 27, units = "cm", res = 300)
plot(plot)
dummy <- dev.off()

pdf("docs/grs/modyX_T2D_p.pdf", height = unit(9, "cm"), width = unit(13.5, "cm"))
plot(plot)
dummy <- dev.off()

x <- c()
y <- c()
z <- c()

for (i in 0:10) {
    
    binMiddleI <- i/10
    binLowI <- (i-0.5)/10
    binHighI <- (i+0.5)/10
    
    for (j in 0:10) {
        
        binMiddleJ <- j/10
        binLowJ <- (j-0.5)/10
        binHighJ <- (j+0.5)/10
        
        n <- sum(modyXMatrix$pT1d > binLowI & modyXMatrix$pT1d <= binHighI
                 & modyXMatrix$pT2d > binLowJ & modyXMatrix$pT2d <= binHighJ)
        
        x <- c(x, binMiddleI)
        y <- c(y, binMiddleJ)
        z <- c(z, n)
        
    }
}

plotDF <- data.frame(x, y, z)
plotDF$color <- ifelse(plotDF$z > 0 & plotDF$z < max(plotDF$z) / 2, 0, 1)
plotDF$color <- factor(plotDF$color)


plot <- ggplot() + theme_bw()

plot <- plot + geom_tile(data = plotDF, aes(x = x, y = y, fill = z)) 
plot <- plot + geom_text(data = plotDF[plotDF$z > 0, ], aes(x = x, y = y, label = z, col = color))

plot <- plot + scale_fill_scico(name = "# Patients", palette = "oslo", direction = -1)
plot <- plot + scale_color_manual(values = c("black", "white"))

plot <- plot + scale_x_continuous(name = "T1D Probability [%]", breaks = (0:10)/10, labels = 10 * (0:10), limits = c(-0.05, 1.05), expand = c(0, 0))
plot <- plot + scale_y_continuous(name = "T2D Probability [%]", breaks = (0:10)/10, labels = 10 * (0:10), limits = c(-0.05, 1.05), expand = c(0, 0))

plot <- plot + theme(legend.position = "none")

png("docs/grs/modyX_T1D_T2D_p.png", height = 18, width = 27, units = "cm", res = 300)
plot(plot)
dummy <- dev.off()

pdf("docs/grs/modyX_T1D_T2D_p.pdf", height = unit(9, "cm"), width = unit(13.5, "cm"))
plot(plot)
dummy <- dev.off()


# Export mody risk scores

if (!anonymized) {
    write.table(modyXMatrix, modyxFile, quote = F, row.names = F, col.names = T, sep = " ")
}
