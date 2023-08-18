import sqlite3

create_lists_stmt = 'CREATE TABLE IF NOT EXISTS `lists`'\
               '(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, '\
               '`title` TEXT NOT NULL, '\
               '`icon` INTEGER NOT NULL, '\
               '`position` REAL NOT NULL)'
insert_lists_stmt = 'INSERT INTO lists (title, icon, position) '\
              'VALUES (:title, :icon, :position)'
check_list_list_rows_stmt = 'SELECT COUNT(*) FROM lists'

list_rows = 5

create_entries_stmt = 'CREATE TABLE IF NOT EXISTS `entries` '\
                    '(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, '\
					'`listId` INTEGER NOT NULL, '\
					'`title` TEXT NOT NULL, '\
					'`type` INTEGER NOT NULL, '\
					'`position` REAL NOT NULL, '\
                    '`metadata` TEXT NOT NULL, '\
					'`status` INTEGER NOT NULL, '\
                    'FOREIGN KEY(`listId`) REFERENCES `lists`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)'
					
insert_entries_stmt = 'INSERT INTO entries (listId, title, type, position, metadata, status) '\
              'VALUES (:listId, :title, :type, :position, :metadata, :status)'

entry_rows = 10

db_filename = 'app\\src\\main\\assets\\test\\database\\testDb' + str(list_rows) + '.db'

def create_metadata(type_int):
    # Film
    if type_int == 0:
        return "{\"type\":0,\"director\":\"director\"}"
    # Show
    elif type_int == 1:
        return "{\"type\":1}"
    # Game
    elif type_int == 2:
        return "{\"type\":2,\"developer\":\"developer\"}"
    # Book
    elif type_int == 3:
        return "{\"type\":3,\"author\":\"author\",\"year_published\":2000}"
    else:
        return "{}"

with sqlite3.connect(db_filename) as db_conn:
    cursor = db_conn.cursor()

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
            metadata = create_metadata(type)

            cursor.execute(insert_entries_stmt, {'listId': list_id, 'title': entry_title, 'type': type, 'position': float(j), 'metadata': metadata, 'status': status})

    cursor.execute(check_list_list_rows_stmt)
    list_rows = cursor.fetchone()
    print('list_rows: ' + str(list_rows[0]))
