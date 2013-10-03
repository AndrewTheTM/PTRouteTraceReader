---
title: Cube Voyager Public Transport Route Trace Parser Tool
layout: default
---
# Cube Voyager Public Transport Route Trace Parser

## Introduction

This is a tool built by OKI to do some analysis on the route trace from a Voyager PT route evaluation process.  The reason this was built is because it seemed to represent the easiest way to get the initial operator of a transit zone pair, which is used in the OKI Mode Choice model (since we have four transit operators in our region, and five total in the model).

The github repository is [here](https://github.com/okiandrew/PTRouteTraceReader).

## Usage

There are a few things that have to be done to use this tool prior to actually running it.  
1. A PT step must be run using REPORTI=z-Z REPORTJ=z-Z on the ROUTEO or ROUTEI files (depending on how you want to do it).  For a model with around 3,000 internal zones, expect the REPORTO file to be around 1.2 GB.
2. A DBF of the route name (used in the transit line files) and the operator is needed.  If the route information is in Geodatabase format, this is pretty easy - use ArcMap or ArcCatalog to export the route PTLine layer to a DBF.  If the route is in a LIN or other format, some text manipulation would be needed.

Once these two items are satisfied, run the tool in a command line using:
java -jar &lt;PTRouteTrace.jar&gt; &lt;line DBF&gt; &lt;REPORTO&gt; &lt;output.dbf&gt;

The resulting DBF for 3000 zones should be around 375 MB, but that assumes a reasonable level of transit coverage and a good network and six different user classes.  More or less transit routes, access, or user class groups would increase or decrease filesize (respectively).

This is open source and offered without any formal support.  Questions can be directed to the author, Andrew Rohne, at arohne@oki.org or @okiAndrew, but no guarantee is made for answers (and especially not the timeliness of answers).

Licensed under GPLv3 2013.

[www.oki.org](www.oki.org)