@echo off

pip install -r requirements.tx > null
call "env\Scripts\activate"
py.exe scraper.py