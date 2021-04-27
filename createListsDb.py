import sqlite3

create_lists_stmt = 'CREATE TABLE IF NOT EXISTS `lists`'\
               '(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, '\
               '`title` TEXT NOT NULL, '\
               '`icon` INTEGER NOT NULL, '\
               '`position` REAL NOT NULL, '\
               '`count` INTEGER NOT NULL)'
insert_lists_stmt = 'INSERT INTO lists (title, icon, position, count) '\
              'VALUES (:title, :icon, :position, 0)'
check_list_list_rows_stmt = 'SELECT COUNT(*) FROM lists'

list_rows = 100

create_entries_stmt = 'CREATE TABLE IF NOT EXISTS `entries` '\
                    '(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, '\
					'`listId` INTEGER NOT NULL, '\
					'`title` TEXT NOT NULL, '\
					'`type` INTEGER NOT NULL, '\
					'`position` REAL NOT NULL, '\
					'`status` INTEGER NOT NULL, '\
                    'FOREIGN KEY(`listId`) REFERENCES `lists`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)'
					
insert_entries_stmt = 'INSERT INTO entries (listId, title, type, position, status) '\
              'VALUES (:listId, :title, :type, :position, :status)'
			  
entry_rows = 10

db_filename = 'app\\src\\main\\assets\\database\\testDb' + str(list_rows) + '.db'

with sqlite3.connect(db_filename) as dbConn:
    cursor = dbConn.cursor()

    cursor.execute(create_lists_stmt)
    cursor.execute(create_entries_stmt)
    
    for i in range(list_rows):
        list_title = 'list' + str(i)
        icon = i % 5
        cursor.execute(insert_lists_stmt, {'title': list_title, 'icon': icon, 'position': float(i)})
		
        for j in range(entry_rows):
            entry_title = 'entry ' + str(j)
            list_id = i + 1
            type = j % 4
            status = j % 3
            cursor.execute(insert_entries_stmt, {'listId': list_id, 'title': entry_title, 'type': type, 'position': float(j), 'status': status})

    cursor.execute(check_list_list_rows_stmt)
    list_rows = cursor.fetchone()
    print('list_rows: ' + str(list_rows[0]))
