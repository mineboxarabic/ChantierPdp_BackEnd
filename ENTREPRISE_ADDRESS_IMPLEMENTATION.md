# Entreprise Address Field Implementation

## Summary of Changes

I have successfully added an `address` attribute to the Entreprise entity and updated all related components:

### 1. Entity Changes
- **File**: `src/main/java/com/danone/pdpbackend/entities/Entreprise.java`
- **Change**: Added `@Column(name = "address") private String address;` field

### 2. DTO Changes  
- **File**: `src/main/java/com/danone/pdpbackend/entities/dto/EntrepriseDTO.java`
- **Change**: Added `private String address;` field

### 3. Mapper Changes
- **File**: `src/main/java/com/danone/pdpbackend/Utils/mappers/EntrepriseMapper.java`
- **Changes**: 
  - Updated `setDTOFields()` method to map `entreprise.getAddress()` to `entrepriseDTO.setAddress()`
  - Updated `setEntityFields()` method to map `entrepriseDTO.getAddress()` to `entreprise.setAddress()`

### 4. Database Schema Changes
- **File**: `src/test/resources/schema.sql`
- **Change**: Added `address varchar(255),` column to the `entreprise` table

## Usage Examples

### Creating an Entreprise with Address

```java
// Using Entity
Entreprise entreprise = Entreprise.builder()
    .nom("Danone")
    .description("Food & Beverage Company")
    .address("17 Boulevard Haussmann, 75009 Paris, France")
    .type(EntrepriseType.EU)
    .build();

// Using DTO
EntrepriseDTO entrepriseDTO = EntrepriseDTO.builder()
    .nom("External Company")
    .description("External contractor")
    .address("123 Main Street, City, Country")
    .type(EntrepriseType.EE)
    .build();
```

### REST API Usage

```json
// POST /api/entreprise
{
    "nom": "Test Company",
    "description": "A test company",
    "address": "456 Business Ave, Commercial District, City 12345",
    "type": "EE",
    "numTel": "123-456-7890",
    "raisonSociale": "Test Corp"
}
```

### Database Storage

The address field is stored in the `entreprise` table with the column name `address` as VARCHAR(255).

## Testing

All existing tests continue to pass, and the new address field is fully functional:

- ✅ Entity can store and retrieve address
- ✅ DTO can handle address field
- ✅ Mapper correctly maps address between entity and DTO
- ✅ Database schema supports address column
- ✅ REST API can create/update/retrieve entreprises with addresses

## Benefits

1. **Complete Address Information**: Entreprises can now store their full address
2. **Backward Compatibility**: All existing code continues to work
3. **Consistent Architecture**: Follows the same pattern as other fields
4. **Full CRUD Support**: Address can be created, read, updated, and deleted through the API

The implementation follows the existing codebase patterns and maintains consistency with other fields in the Entreprise entity.
