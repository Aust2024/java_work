#匯入CSV檔案進入mySQL資料表

#os模組：控制和操作電腦內的文件操作電腦功能
import os
import pandas as pd

#使用mysqlclient庫
import MySQLdb

#MySQL連線資訊
db_host = 'localhost'
db_user = 'root'
db_password = 'ab901087'
db_name = 'world'
db_table = 'produce'

#資料夾路徑
folder_path = r'D:\作業\18號\date'

#建立MySQL連線
conn = MySQLdb.connect(
    host=db_host,
    user=db_user,
    passwd=db_password,
    db=db_name
)
cursor = conn.cursor()

#獲取資料表的欄位名稱
cursor.execute(f"SHOW COLUMNS FROM {db_table}")
table_columns = [column[0] for column in cursor.fetchall()]

#循環資料夾中的所有CSV檔案
for filename in os.listdir(folder_path):
    if filename.endswith('.csv'):
        file_path = os.path.join(folder_path, filename)
        print(f'正在匯入檔案: {file_path}')
        
        #讀取CSV檔案
        df = pd.read_csv(file_path)
        
        #清理列名，移除空格或特殊字符
        df.columns = df.columns.str.replace('[(){}<>]', '', regex=True)
        df.columns = df.columns.str.replace(' ', '_')  # 用下劃線代替空格
        
        #確保數值欄位為正確的數字型別
        numeric_columns = ['highest_price', 'median_price', 'lowest_price',
                           'average_price', 'transaction_volume']
        for col in numeric_columns:
            df[col] = pd.to_numeric(df[col].replace({',': ''}, regex=True),
                                    errors='coerce')
        
        #只保留與資料表匹配的欄位
        df = df[[col for col in df.columns if col in table_columns]]
        
        #將NaN值轉換為None，這樣MySQL可以接受它作為NULL
        df = df.where(pd.notnull(df), None)
        
        #將DataFrame寫入MySQL資料表
        for i, row in df.iterrows():
            # 使用反引號包裹列名
            columns = ', '.join([f'`{col}`' for col in df.columns])
            sql = f"""INSERT INTO {db_table} ({columns}) VALUES ({
            ', '.join(['%s'] * len(row))})"""
            cursor.execute(sql, tuple(row))

        #提交變更
        conn.commit()

#關閉資料庫連線
cursor.close()
conn.close()

print("所有 CSV 檔案已成功匯入 MySQL 資料表。")



