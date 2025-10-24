-- Удаление лишней колонки stock_ticker из таблицы futures
-- Эта колонка не используется в коде и была создана по ошибке

DO $$
BEGIN
    -- Проверяем, существует ли колонка stock_ticker в таблице futures
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_schema = 'invest' 
               AND table_name = 'futures' 
               AND column_name = 'stock_ticker') THEN
        
        -- Удаляем колонку
        ALTER TABLE invest.futures DROP COLUMN stock_ticker;
        
        RAISE NOTICE 'Колонка stock_ticker успешно удалена из таблицы invest.futures';
    ELSE
        RAISE NOTICE 'Колонка stock_ticker не найдена в таблице invest.futures';
    END IF;
END $$;

