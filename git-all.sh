#!/bin/bash

echo "Project: Green"
cd Green
$*
echo "Project: GreenAssociationRelationship"
cd ../GreenAssociationRelationship
$*
echo "Project: GreenCompositionRelationship"
cd ../GreenCompositionRelationship
$*
echo "Project: GreenDependencyRelationship"
cd ../GreenDependencyRelationship
$*
echo "Project: GreenFeature"
cd ../GreenFeature
$*
echo "Project: GreenGeneralizationRelationship"
cd ../GreenGeneralizationRelationship
$*
echo "Project: GreenHelp"
cd ../GreenHelp
$*
echo "Project: GreenRealizationRelationship"
cd ../GreenRealizationRelationship
$*
echo "Project: GreenRelationshipsFeature"
cd ../GreenRelationshipsFeature
$*
echo "Project: GreenSVGSave"
cd ../GreenSVGSave
$*
echo "Project: GreenSVGSaveFeature"
cd ../GreenSVGSaveFeature
$*
echo "Project: GreenUpdateSite"
cd ../GreenUpdateSite
$*
cd ..
