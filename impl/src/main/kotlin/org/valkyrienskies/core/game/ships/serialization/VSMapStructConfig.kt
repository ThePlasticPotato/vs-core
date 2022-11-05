package org.valkyrienskies.core.game.ships.serialization

import org.mapstruct.InjectionStrategy.CONSTRUCTOR
import org.mapstruct.MapperConfig
import org.mapstruct.MappingConstants.ComponentModel

// Configures MapStruct to use Dagger for injection
@MapperConfig(
    // use forked version of MapStruct for Dagger2 compatible injection
    // (generates empty constructors annotated with @Inject)
    componentModel = ComponentModel.DAGGER2,
    injectionStrategy = CONSTRUCTOR
)
internal class VSMapStructConfig
