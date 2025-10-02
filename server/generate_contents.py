#!/usr/bin/env python3

import argparse
import re
import sys
from requests import post
from textwrap import fill
from quest_for_gpt import *
from _main import *
parser = argparse.ArgumentParser()

#file = open(direct)
file = None
ask = ChatRequest
print(ask)
ARGS = [
    ('-r', '--role',        str,   role, 'Role of generator'),
    ('-a', '--ask',         str,   ask,      'Text of question'),
    ('-f', '--file',        str,   file,                   'File with question'),
    ('-t', '--temperature', float, level_of_creative,                    '"Temperature" of model'),
    ('-w', '--wrap-at',     int,   None,                   'Wrap at this symbol'),
    ('-i', '--ident',       int,   None,                   'Ident spaces'),
    ('-of', '--output-file', str, file_name, 'File to write JSON output'),
    ('-T', '--title', str, title, 'Title of the text'),
]

def parse_args(parser, args):
    for a_short, a_long, a_type, a_value, a_help in args:
        parser.add_argument(a_short, a_long, type=a_type, default=a_value, help=a_help)

    args = parser.parse_args()
    print(args)

    role = args.role
    ask = args.ask
    file = args.file
    temperature = args.temperature
    wrap_at = args.wrap_at
    output_file = args.output_file
    try:
        ident = ' ' * args.ident
    except TypeError:
        ident = args.ident

    try:
        ask = open(file).read() if file is not None else ask
    except IOError as e:
        raise Exception(f'File {file} asked but {e}')

    return role, ask, temperature, wrap_at, ident, output_file
    

def create_data(role, ask, temperature, folder_id):
    data = {
        "modelUri": f"gpt://{folder_id}/yandexgpt-lite",
        "modelUri": f"gpt://{folder_id}/yandexgpt/rc",
        "completionOptions": {
        "stream": False,
        "temperature": temperature,
        "maxTokens": "5000"
        },
        "messages": [
        {
            "role": "system",
            "text": role
        },
        {
            "role": "user",
            "text": ask
        }
        ]
    }
    return data

def post_to_yc(data, iam_token, folder_id):
    x = post(
        "https://llm.api.cloud.yandex.net/foundationModels/v1/completion",
        json=data, 
        headers={
            "Content-Type": "application/json",
            "Authorization": f"Bearer { iam_token }",
            "x-folder-id": f"{ folder_id }" ,
        },
        timeout=50,
    ).json()

    return x


def parse_rez(rez, wrap_at, ident):

    alternatives = rez['result']['alternatives']
    text = ""
    for alternative in alternatives:
        _in = alternative['message']['text']
        _in = _in.replace(':**', '.').replace('**', '')
        text += _in

    if wrap_at is None or ident is None:
        out = text
    else:
        out = ''
        for p in text.split('\n\n'):
            pp = fill(text=p, width=wrap_at)
            for l in pp.split('\n'):
                out += f'{ident}{l}\n'
            out += '\n'

    return out


if __name__ == '__main__':
    role, ask, temperature, wrap_at, ident, output_file = parse_args(argparse.ArgumentParser(), ARGS)
    data = create_data(role, ask, temperature, folder_id)
    rez = post_to_yc(data, iam_token, folder_id)
    to_print = parse_rez(rez, wrap_at, ident)
    open(output_file, 'w').write(to_print)