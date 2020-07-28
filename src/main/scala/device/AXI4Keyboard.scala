/**************************************************************************************
* Copyright (c) 2020 Institute of Computing Technology, CAS
* Copyright (c) 2020 University of Chinese Academy of Sciences
* 
* NutShell is licensed under Mulan PSL v2.
* You can use this software according to the terms and conditions of the Mulan PSL v2. 
* You may obtain a copy of Mulan PSL v2 at:
*             http://license.coscl.org.cn/MulanPSL2 
* 
* THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER 
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR 
* FIT FOR A PARTICULAR PURPOSE.  
*
* See the Mulan PSL v2 for more details.  
***************************************************************************************/

package device

import chisel3._
import chisel3.util._

import bus.axi4._
import utils._

class KeyboardIO extends Bundle {
  val ps2Clk = Input(Bool())
  val ps2Data = Input(Bool())
}

// this Module is not tested
class AXI4Keyboard extends AXI4SlaveModule(new AXI4Lite, new KeyboardIO) {
  val buf = Reg(UInt(10.W))
  val ps2ClkLatch = RegNext(io.extra.get.ps2Clk)
  val negedge = RegNext(ps2ClkLatch) && ~ps2ClkLatch
  when (negedge) { buf := Cat(io.extra.get.ps2Data, buf(9,1)) }

  val cnt = Counter(negedge, 10)
  val queue = Module(new Queue(UInt(8.W), 8))
  queue.io.enq.valid := cnt._2 && !buf(0) && io.extra.get.ps2Data && buf(9,1).xorR
  queue.io.enq.bits := buf(8,1)
  queue.io.deq.ready := in.r.ready

  in.r.bits.data := Mux(queue.io.deq.valid, queue.io.deq.bits, 0.U)
}
