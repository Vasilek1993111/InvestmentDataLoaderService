-- Добавление поля basicAssetSize в таблицу futures
-- Поле хранит размер базового актива фьючерса в формате BigDecimal

-- Добавляем колонку basicAssetSize в таблицу futures
ALTER TABLE invest.futures 
ADD COLUMN basic_asset_size DECIMAL(18,9);

-- Добавляем комментарий к колонке
COMMENT ON COLUMN invest.futures.basic_asset_size IS 'Размер базового актива фьючерса (basicAssetSize из T-Invest API)';

-- Обновляем updated_at для всех существующих записей
UPDATE invest.futures 
SET updated_at = CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Moscow'::text
WHERE basic_asset_size IS NULL;

