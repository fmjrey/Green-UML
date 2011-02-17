#!/bin/bash
git cvsimport -v -d :pserver:anonymous@green.cvs.sourceforge.net:/cvsroot/green -C Green -r cvs -k Green
git cvsimport -v -d :pserver:anonymous@green.cvs.sourceforge.net:/cvsroot/green -C GreenAssociationRelationship -r cvs -k GreenAssociationRelationship
git cvsimport -v -d :pserver:anonymous@green.cvs.sourceforge.net:/cvsroot/green -C GreenCompositionRelationship -r cvs -k GreenCompositionRelationship
git cvsimport -v -d :pserver:anonymous@green.cvs.sourceforge.net:/cvsroot/green -C GreenDependencyRelationship -r cvs -k GreenDependencyRelationship
git cvsimport -v -d :pserver:anonymous@green.cvs.sourceforge.net:/cvsroot/green -C GreenFeature -r cvs -k GreenFeature
git cvsimport -v -d :pserver:anonymous@green.cvs.sourceforge.net:/cvsroot/green -C GreenGeneralizationRelationship -r cvs -k GreenGeneralizationRelationship
git cvsimport -v -d :pserver:anonymous@green.cvs.sourceforge.net:/cvsroot/green -C GreenHelp -r cvs -k GreenHelp
git cvsimport -v -d :pserver:anonymous@green.cvs.sourceforge.net:/cvsroot/green -C GreenRealizationRelationship -r cvs -k GreenRealizationRelationship
git cvsimport -v -d :pserver:anonymous@green.cvs.sourceforge.net:/cvsroot/green -C GreenRelationshipsFeature -r cvs -k GreenRelationshipsFeature
git cvsimport -v -d :pserver:anonymous@green.cvs.sourceforge.net:/cvsroot/green -C GreenSVGSave -r cvs -k GreenSVGSave
git cvsimport -v -d :pserver:anonymous@green.cvs.sourceforge.net:/cvsroot/green -C GreenSVGSaveFeature -r cvs -k GreenSVGSaveFeature
git cvsimport -v -d :pserver:anonymous@green.cvs.sourceforge.net:/cvsroot/green -C GreenUpdateSite -r cvs -k GreenUpdateSite
