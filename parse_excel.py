#!/usr/bin/python

import sys
import json
sys.path.append(sys.path[0] + "/lib")

import openpyxl
import sqlite3

if len(sys.argv) != 2:
		print "usage: python parse-excel.py keywords-file.xlsx old.json"
		sys.exit(-1)

wb = openpyxl.load_workbook(filename = sys.argv[1])

master_sheet = wb.get_sheet_by_name("Master")
master_rows = master_sheet.range('A3:S143')

agencies = []

for row in master_rows:
    name = row[2].value
    mother_goose = row[16].value
    nobodys_perfect = row[17].value
    triple_p = row[18].value

    events = []

    if mother_goose:
        events.append("Mother Goose")

    if nobodys_perfect:
        events.append("Nobody's Perfect")

    if triple_p:
        events.append("Triple P Parenting")

    agency = {
        "name" : name,
        "events" : events
    }

    agencies.append(agency)

f = open('stuff.txt', 'w+')
f.write(json.dumps(agencies, indent=4))