package deeplearning

import chisel3._
import chisel3.util._

class OutputLayer(
    dtype: SInt,
    inNo: Int = 10,
) extends Module {

  val io = IO(new Bundle() {
    val dataIn = Flipped(Decoupled(Vec(inNo, dtype)))
    val dataOut = Decoupled(UInt(log2Ceil(inNo).W))
  })

  val result_bits: Int = log2Ceil(inNo)
  val latency: Int = result_bits
  var result: List[UInt] = (0 until inNo)
    .map(i => {
      i.asUInt(result_bits.W)
    })
    .toList

  def comparator(a: UInt, b: UInt): UInt = {
    val bigger = Mux(io.dataIn.bits(a) > io.dataIn.bits(b), a, b)
    bigger
  }

  while (result.size > 1) {
    result = result
      .grouped(2)
      .map(grp => {
        val big = grp.reduce(comparator)
        RegNext(big)
      })
      .toList
  }

  io.dataIn.ready := true.B
  io.dataOut.valid := ShiftRegister(io.dataIn.valid,latency,false.B,true.B)
  io.dataOut.bits := result.head
}
