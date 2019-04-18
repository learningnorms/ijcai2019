Convergence Plots 

Basic Version: A_base, B_base C_base.csv

iv.setSensorReliability(0.99);
iv.setPropMissing(0.1);
iv.setProbIgnorance(0.1);
iv.setSanctionRate(1.0);
iv.setLifetime(50);
iv.setFlatness(3.0);
iv.setNumObservers(1);
iv.setNumActors(100);
iv.setNumNorms(3);
iv.setMasterSeed(0);
m = 10


No Sanction (A_nosanc)
iv.setSensorReliability(0.99);
iv.setPropMissing(0.1);
iv.setProbIgnorance(0.1);
iv.setSanctionRate(0.1);
iv.setLifetime(50);
iv.setFlatness(3.0);
iv.setNumObservers(1);
iv.setNumActors(100);
iv.setNumNorms(3);
iv.setMasterSeed(0);

No Compliance + No sanctioning.  A_nocomp
iv.setSensorReliability(0.99);
iv.setPropMissing(0.1);
iv.setProbIgnorance(0.1);
iv.setSanctionRate(0.1);
iv.setLifetime(50);
iv.setFlatness(0.1);
iv.setNumObservers(1);
iv.setNumActors(100);
iv.setNumNorms(3);
iv.setMasterSeed(0);

Ambiguity A_am
iv.setSensorReliability(0.99);
iv.setPropMissing(0.5);
iv.setProbIgnorance(0.5);
iv.setSanctionRate(1.0);
iv.setLifetime(50);
iv.setFlatness(3.0);
iv.setNumObservers(1);
iv.setNumActors(100);
iv.setNumNorms(3);
iv.setMasterSeed(0);

Sensor Reliability A_rel
iv.setSensorReliability(0.5);
iv.setPropMissing(0.1);
iv.setProbIgnorance(0.1);
iv.setSanctionRate(1.0);
iv.setLifetime(50);
iv.setFlatness(3.0);
iv.setNumObservers(1);
iv.setNumActors(100);
iv.setNumNorms(3);
iv.setMasterSeed(0);



