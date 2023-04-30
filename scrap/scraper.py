from selenium import webdriver
import time
from selenium.webdriver.common.by import By
import json
from datetime import datetime

URL = "https://www.codingame.com/profile/cfaba466914c8a1e446b26a2c1aca1df1812384"

def scrap():
    
    options = webdriver.ChromeOptions()
    options.add_argument("--window-size=1920,1080")
    
    driver = webdriver.Chrome(options=options)

    print("open root url...")
    driver.get(URL)
    time.sleep(10)

    print("load ranking window...")
    driver.find_element(By.CLASS_NAME, "container-0-1-103").click()
    time.sleep(30)

    print("scrap wrappers...")
    wrappers = driver.find_elements(By.CSS_SELECTOR, ".detail-wrapper > div")

    data = {}

    for wrapper in wrappers:

        wrapper_name = wrapper.find_element(By.CSS_SELECTOR, ".coding-points-title").text
        wrapper_cp = wrapper.find_element(By.CSS_SELECTOR, ".coding-points-cp").text
        wrapper_ranks = wrapper.find_elements(By.CSS_SELECTOR, ".profile-ranking-tooltip > div")
        wrapper_global_rank = wrapper_ranks[1].find_elements(By.TAG_NAME, "p")[2].get_attribute('innerText')

        try:
            wrapper_local_rank = wrapper_ranks[2].find_elements(By.TAG_NAME, "p")[2].get_attribute('innerText')
        except Exception:
            wrapper_local_rank = "N/A"

        data[wrapper_name] = {
            "global": wrapper_global_rank,
            "local": wrapper_local_rank,
            "cp": wrapper_cp,
            "rows": [] 
        }

        print("scrapping %s... (global = %s, local = %s, cp = %s)" % (wrapper_name, wrapper_global_rank, wrapper_local_rank, wrapper_cp))
        wrapper_graph = wrapper.find_element(By.CSS_SELECTOR, ".coding-points-graph-container")
        wrapper_graph.click()
        time.sleep(2)

        detail_rows = wrapper.find_elements(By.CSS_SELECTOR, ".recent-activity tr")[1:]

        for row in detail_rows:

            columns = row.find_elements(By.TAG_NAME, "td")
            titles = columns[0].find_elements(By.CSS_SELECTOR, "div, span")
            title = " ".join([t.get_attribute('innerText') for t in titles])
            cp = columns[2].find_element(By.TAG_NAME, "span").get_attribute('innerText')
            ranks = columns[1].find_elements(By.TAG_NAME, "span")
            rank = ranks[0].get_attribute('innerText')
            pool = ranks[1].get_attribute('innerText')

            data[wrapper_name]['rows'].append({
                "title": title,
                "rank": "%s / %s" % (rank, pool),
                "cp": cp
            })

            print("%s, %s / %s, %s" % (title, rank, pool, cp))
    
    return data


data = scrap()

scraped = json.loads(open("data.json", "r").read())
scraped[datetime.now().strftime("%Y/%m/%d %H:%M:%S")] = data

with open("data.json", "w") as file:
    file.write(json.dumps(scraped, indent=4))
    file.close()
