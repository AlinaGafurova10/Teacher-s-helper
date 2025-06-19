# Получение из запроса пользователя (или указать значения по умолчанию)
role = "Учитель" # Роль пользователя
ask = "Создай тест по физике на 15 вопросов" # Запрос к ИИ
direct = None # Место хранения файла
file_name = "Ответ.json" # Название файла
title = "Тест" # Заголовок
level_of_creative = float(0.1) #Можно добавить ползунок

#!/bin/bash (Вводится в консоль)
"""
export FOLDER_ID=xxxxxxxxxxxxxxxxxxx
yc iam create-token > ~/.iam_token
"""

import subprocess


def get_ask(asf):
    ask = asf
print(ask)
#command = "yc iam create-token > ~/.iam_token"
command = "yc iam create-token"
iam_token = "t1.9euelZqSlo3JyY3PyM2Wl8yNip2Wmu3rnpWazJieipHKyIzOmsvNm5LJxo_l8_dcWzE9-e8UTQww_t3z9xwKLz357xRNDDD-zef1656VmpyblMzOl5ucm5GZj4menJXM7_zF656VmpyblMzOl5ucm5GZj4menJXM.5c5jR3KV8KprcFjfmIzwLACaPgJByuNZUQbzrQ_BjlFDAviFXb3PIQLXr98-rOQYILwlZlq628-RtaLSGqgPBQ"

try:
    result = subprocess.run(command, shell=True, check=True, capture_output=True, text=True)
    iam_token = result
except subprocess.CalledProcessError as e:
    print(f"Ошибка: {e}")
    print(e.stderr)
except IOError as e:
    print("DEBUG: YOU CAN'T GET IAM-TOKEN, PLEASE TRY AGAIN")
    raise Exception(f'IAM_TOKEN not found with {e}')

folder_id = 'b1goanfad3n1392h6v76' # Не меняется (ID серверного бота, отправляющего запросы)
