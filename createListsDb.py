import sqlite3

create_stmt = 'CREATE TABLE IF NOT EXISTS `lists`'\
               '(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, '\
               '`title` TEXT NOT NULL, '\
               '`icon` INTEGER NOT NULL, '\
               '`position` REAL NOT NULL, '\
               '`count` INTEGER NOT NULL)'
insert_stmt = 'INSERT INTO lists (title, icon, position, count) '\
              'VALUES (:title, :icon, :position, 0)'
check_rows_stmt = 'SELECT COUNT(*) FROM lists'

rows = 100000

db_filename = 'app\\src\\main\\assets\\database\\testDb' + str(rows) + '.db'

with sqlite3.connect(db_filename) as dbConn:
    cursor = dbConn.cursor()

    cursor.execute(create_stmt)
    
    for i in range(rows):
        list_title = 'list' + str(i)
        icon = i % 5
        cursor.execute(insert_stmt, {'title': list_title, 'icon': icon, 'position': float(i)})
        
    cursor.execute(check_rows_stmt)
    rows = cursor.fetchone()
    print('rows: ' + str(rows[0]))
