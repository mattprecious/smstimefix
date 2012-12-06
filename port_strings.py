#!/usr/bin/python

import xml.etree.ElementTree as etree
import xml.dom.minidom as minidom
import glob
import os
import sys

def makedir(directory):
	if not os.path.exists(directory):
		os.makedirs(directory)

def findandappend(source, target):
	nodes = source.findall(".//string[@multiapk='true']")
	for node in nodes:
		node.attrib.pop('multiapk')
		target.append(node)

def indent(elem, level=0):
    i = "\n" + level*"\t"
    if len(elem):
        if not elem.text or not elem.text.strip():
            elem.text = i + "\t"
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
        for elem in elem:
            indent(elem, level+1)
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
    else:
        if level and (not elem.tail or not elem.tail.strip()):
            elem.tail = i

def generatefile(sources, targets):
	root = etree.Element('resources')
	for source in sources:
		findandappend(etree.parse(source), root)

	indent(root)
	output = etree.tostring(root)

	for target in targets:
		f = open(target, 'wb')
		f.write(bytes('<?xml version="1.0" encoding="UTF-8"?>\n', 'utf-8'))
		f.write(output)
		f.close()

rootPath = sys.path[0] + '/'

resLibrary = rootPath + 'SMSFixLibrary/res/'
resRegular = rootPath + 'SMSFix/res/'
resDonate = rootPath + 'SMSFixDonate/res/'

valuesLibrary = resLibrary + 'values/'
valuesRegular = resRegular + 'values/'
valuesDonate = resDonate + 'values/'

# english (with universal file)
generatefile({valuesLibrary + "strings-universal.xml", valuesLibrary + "strings.xml"}, {valuesRegular + "strings.xml", valuesDonate + "strings.xml"})

# the rest
for directory in glob.glob(resLibrary + 'values-*'):
	valuesDir = directory[len(resLibrary):]
	
	makedir(resRegular + valuesDir)
	makedir(resDonate + valuesDir)

	generatefile({directory + '/strings.xml'}, {resRegular + valuesDir + '/strings.xml', resDonate + valuesDir + '/strings.xml'})