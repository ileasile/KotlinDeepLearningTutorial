package tensorflow.old_api.inference

import org.tensorflow.*
import util.MnistUtils
import java.util.*

const val IMAGE_PATH = "src/main/resources/datasets/test/t10k-images-idx3-ubyte"
const val LABEL_PATH = "src/main/resources/datasets/test/t10k-labels-idx1-ubyte"
const val PATH_TO_MODEL = "src/main/resources/model1"

fun main() {
    val images = MnistUtils.mnistAsList(IMAGE_PATH, LABEL_PATH, Random(0), 10000)

    fun reshape(doubles: DoubleArray): Tensor<*>? {
        val reshaped = Array(
            1
        ) { Array(28) { FloatArray(28) } }
        for (i in doubles.indices) reshaped[0][i / 28][i % 28] = doubles[i].toFloat()
        return Tensor.create(reshaped)
    }

    predictOnImagesWithTensor(images, ::reshape)
}

private fun predictOnImagesWithTensor(
    images: MutableList<MnistUtils.MnistLabeledImage>,
    reshape: (DoubleArray) -> Tensor<*>?
) {
    SavedModelBundle.load(PATH_TO_MODEL, "serve").use { bundle ->
        val session = bundle.session()

        var counter = 0

        for (image in images) {
            val runner = session.runner()
            val result = runner.feed("Placeholder", reshape(image.pixels))
                .fetch("ArgMax")
                .run()[0]
                .copyTo(LongArray(1))
            if (result[0].toInt() == image.label)
                counter++

        }
        println(counter)
        println(images.size)

        session.close()
    }
}