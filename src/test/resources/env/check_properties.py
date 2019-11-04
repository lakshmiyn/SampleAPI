#!/usr/bin/env python
# -*- coding: utf-8 -*-

import re
from glob import glob
from collections import defaultdict
import pprint

# Set this to True to add Placeholders
ADD_FIXME = False

PROPERTY_REGEX = re.compile(r'^([\w.]+)=(.*)$')
COMMENT_REGEX = re.compile(r'^# (.*)$')

all_properties = set()
incomplete_properties = set()
env_properties = defaultdict(dict)
common_properties = defaultdict(set)
missing_values_per_env = defaultdict(set)

ENVIRONMENTS = [
        "dev",
        "int",
        "rel",
        "mas",
        "stg"
        ]

def load_properties(env):
    props = dict()
    with open('{}.properties'.format(env), 'r') as propfile:
        for line in propfile:
            m = PROPERTY_REGEX.match(line)
            if m:
                props[m.group(1)] = m.group(2)
    return props


def main():
    # Read all environment properties
    for env in ENVIRONMENTS:
        env_properties[env] = load_properties(env)
        all_properties.update(env_properties[env].keys())

    # Check for properties that are missing in some files
    for prop_name in all_properties:
        for env in ENVIRONMENTS:
            if prop_name not in env_properties[env]:
                missing_values_per_env[env].add(prop_name)
                print "{} not found in {}".format(prop_name, env)
            common_properties[prop_name].add(env_properties[env][prop_name])

    # Check for properties that never change
    for prop_name, values in common_properties.items():
        if len(values) == 1 and prop_name not in incomplete_properties:
            print "Common property: {}={}".format(prop_name, list(values)[0])


    # Optionally add "prop=FIXME" for missing properties
    if ADD_FIXME:
        for env, props in missing_values_per_env.items():
            with open('{}.properties'.format(env), 'a') as propfile:
                for prop_name in sorted(props):
                    propfile.write("{}=FIXME\n".format(prop_name))


if __name__ == '__main__':
    main()

