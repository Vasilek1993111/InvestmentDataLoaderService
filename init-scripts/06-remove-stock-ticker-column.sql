-- Удаление столбца stock_ticker из таблицы futures
-- Этот скрипт удаляет столбец stock_ticker, который больше не используется

-- Удаляем столбец stock_ticker, если он существует
ALTER TABLE invest.futures DROP COLUMN IF EXISTS stock_ticker;

-- Проверяем, что столбец удален
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_schema = 'invest' 
        AND table_name = 'futures' 
        AND column_name = 'stock_ticker'
    ) THEN
        RAISE NOTICE 'Столбец stock_ticker все еще существует';
    ELSE
        RAISE NOTICE 'Столбец stock_ticker успешно удален';
    END IF;
END $$;

-- Выводим информацию о текущей структуре таблицы
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_schema = 'invest' 
AND table_name = 'futures'
ORDER BY ordinal_position;
