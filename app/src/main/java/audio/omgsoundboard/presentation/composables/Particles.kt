package audio.omgsoundboard.presentation.composables

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import me.nikhilchaudhari.quarks.CreateParticles
import me.nikhilchaudhari.quarks.particle.*

@Composable
fun Particles(){
    CreateParticles(
        modifier = Modifier.fillMaxSize(),
        x = 500f, y = -50f,
        velocity = Velocity(xDirection = 1f, yDirection = 1f, randomize = true),
        force = Force.Gravity(0.01f),
        acceleration = Acceleration(),
        particleSize = ParticleSize.RandomSizes(5..20),
        particleColor = ParticleColor.RandomColors(listOf(Color.Yellow, Color.White)),
        lifeTime = LifeTime(255f, 0.01f),
        emissionType = EmissionType.FlowEmission(maxParticlesCount = EmissionType.FlowEmission.INDEFINITE, emissionRate = 0.4f),
    )
}